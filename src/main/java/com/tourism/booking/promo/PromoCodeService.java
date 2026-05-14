package com.tourism.booking.promo;

import com.tourism.booking.exception.BusinessException;
import com.tourism.booking.PagedResponse;
import com.tourism.booking.exception.ResourceNotFoundException;
import com.tourism.booking.audit.AuditAction;
import com.tourism.booking.audit.AuditLogService;
import com.tourism.booking.promo.dto.PromoCodeRequest;
import com.tourism.booking.promo.dto.PromoCodeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public PagedResponse<PromoCodeResponse> browsePromoCodes(Pageable pageable) {

        Page<PromoCode> page = promoCodeRepository.findAll(pageable);

        List<PromoCodeResponse> content = page.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return PagedResponse.from(page, content);
    }

    @Transactional(readOnly = true)
    public PromoCodeResponse getById(Long id) {
        return toResponse(promoCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PromoCode", id)));
    }

    @Transactional
    public PromoCodeResponse create(PromoCodeRequest request) {
        if (promoCodeRepository.findByCodeIgnoreCase(request.getCode()).isPresent()) {
            throw new BusinessException("Promo code already exists");
        }
        PromoCode entity = fromRequest(request, new PromoCode());
        entity = promoCodeRepository.save(entity);
        auditLogService.log(AuditAction.PROMO_CODE_CREATED, "PROMO_CODE", String.valueOf(entity.getId()),
                "code=" + entity.getCode());
        return toResponse(entity);
    }

    @Transactional
    public PromoCodeResponse update(Long id, PromoCodeRequest request) {
        PromoCode entity = promoCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PromoCode", id));
        promoCodeRepository.findByCodeIgnoreCase(request.getCode()).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new BusinessException("Another promo uses this code");
            }
        });

        entity = promoCodeRepository.save(fromRequest(request, entity));
        auditLogService.log(AuditAction.PROMO_CODE_UPDATED, "PROMO_CODE", String.valueOf(id),
                "code=" + entity.getCode());
        return toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        PromoCode entity = promoCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PromoCode", id));
        promoCodeRepository.delete(entity);
        auditLogService.log(AuditAction.PROMO_CODE_DELETED, "PROMO_CODE", String.valueOf(id),
                "code=" + entity.getCode());
    }

    /**
     * Validates promo against business rules and returns discount amount (zero if no code).
     */
    @Transactional(readOnly = true)
    public PromoPricingResult applyPromo(String rawCode, BigDecimal originalAmount, LocalDate referenceDate) {
        if (!StringUtils.hasText(rawCode)) {
            return PromoPricingResult.noPromo(originalAmount);
        }
        String code = rawCode.trim();
        PromoCode promo = promoCodeRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new BusinessException("Invalid promo code"));

        validatePromo(promo, originalAmount, referenceDate);

        BigDecimal discount = computeDiscount(promo, originalAmount);
        BigDecimal finalAmount = originalAmount.subtract(discount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        // 😂😂😂😂
        discount = originalAmount.subtract(finalAmount).setScale(2, RoundingMode.HALF_UP);

        return new PromoPricingResult(
                originalAmount,
                discount,
                finalAmount,
                promo.getCode(),
                "Promo applied"
        );
    }

    // applyPromo → حساب فقط
    // incrementUsage → بعد الدفع الحقيقي
    @Transactional
    public void incrementUsageIfPresent(String rawCode) {
        if (!StringUtils.hasText(rawCode)) {
            return;
        }
        PromoCode promo = promoCodeRepository.findByCodeIgnoreCaseForUpdate(rawCode.trim())
                .orElse(null);
        if (promo == null) {
            return;
        }
        if (promo.getMaxUses() != null && promo.getCurrentUses() >= promo.getMaxUses()) {
            throw new BusinessException("Promo code usage limit reached");
        }
        promo.setCurrentUses(promo.getCurrentUses() + 1);
        promoCodeRepository.save(promo);
    }

    public void validatePromo(PromoCode promo, BigDecimal originalAmount, LocalDate referenceDate) {
        if (!promo.isActive()) {
            throw new BusinessException("Promo code is not active");
        }
        if (referenceDate.isBefore(promo.getValidFrom()) || referenceDate.isAfter(promo.getValidTo())) {
            throw new BusinessException("Promo code is not valid for these dates");
        }
        if (originalAmount.compareTo(promo.getMinBookingAmount()) < 0) {
            throw new BusinessException("Booking amount is below minimum for this promo");
        }
        if (promo.getMaxUses() != null && promo.getCurrentUses() >= promo.getMaxUses()) {
            throw new BusinessException("Promo code has no remaining uses");
        }
    }

    private BigDecimal computeDiscount(PromoCode promo, BigDecimal originalAmount) {
        if (promo.getDiscountType() == DiscountType.PERCENTAGE) {
            if (promo.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
                throw new BusinessException("Invalid percentage discount");
            }
            return originalAmount.multiply(promo.getDiscountValue())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
        return promo.getDiscountValue().setScale(2, RoundingMode.HALF_UP);
    }

    private PromoCode fromRequest(PromoCodeRequest request, PromoCode entity) {
        entity.setCode(request.getCode().trim());
        entity.setDiscountType(request.getDiscountType());
        entity.setDiscountValue(request.getDiscountValue());
        entity.setValidFrom(request.getValidFrom());
        entity.setValidTo(request.getValidTo());
        entity.setMinBookingAmount(request.getMinBookingAmount());
        entity.setMaxUses(request.getMaxUses());
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        } else if (entity.getId() == null) {
            entity.setActive(true);
        }
        return entity;
    }

    private PromoCodeResponse toResponse(PromoCode p) {
        return PromoCodeResponse.builder()
                .id(p.getId())
                .code(p.getCode())
                .discountType(p.getDiscountType())
                .discountValue(p.getDiscountValue())
                .validFrom(p.getValidFrom())
                .validTo(p.getValidTo())
                .minBookingAmount(p.getMinBookingAmount())
                .maxUses(p.getMaxUses())
                .currentUses(p.getCurrentUses())
                .active(p.isActive())
                .build();
    }

    public record PromoPricingResult(
            BigDecimal originalAmount,
            BigDecimal discountAmount,
            BigDecimal finalAmount,
            String appliedPromoCode,
            String message
    ) {
        public static PromoPricingResult noPromo(BigDecimal original) {
            BigDecimal o = original.setScale(2, RoundingMode.HALF_UP);
            return new PromoPricingResult(o, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), o, null, "No promo applied");
        }
    }
}

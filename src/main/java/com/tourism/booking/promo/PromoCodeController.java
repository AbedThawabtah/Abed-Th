package com.tourism.booking.promo;

import com.tourism.booking.PagedResponse;
import com.tourism.booking.promo.dto.PromoCodeRequest;
import com.tourism.booking.promo.dto.PromoCodeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Tag(name = "Promo codes", description = "Discount codes (manager/admin)")
@RestController
@RequestMapping("/api/promo-codes")
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    @Operation(summary = "Browse promo codes with pagination")
    @GetMapping
    public ResponseEntity<PagedResponse<PromoCodeResponse>> browse(

            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(promoCodeService.browsePromoCodes(pageable));
    }

    @Operation(summary = "Get promo code by id")
    @GetMapping("/{id}")
    public ResponseEntity<PromoCodeResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(promoCodeService.getById(id));
    }

    @Operation(summary = "Create promo code")
    @PostMapping
    public ResponseEntity<PromoCodeResponse> create(
            @Valid @RequestBody PromoCodeRequest request,
            UriComponentsBuilder uriBuilder) {
        PromoCodeResponse body = promoCodeService.create(request);
        URI location = uriBuilder.path("/api/promo-codes/{id}").buildAndExpand(body.getId()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @Operation(summary = "Update promo code")
    @PutMapping("/{id}")
    public ResponseEntity<PromoCodeResponse> update(@PathVariable Long id, @Valid @RequestBody PromoCodeRequest request) {
        return ResponseEntity.ok(promoCodeService.update(id, request));
    }

    @Operation(summary = "Delete promo code")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        promoCodeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

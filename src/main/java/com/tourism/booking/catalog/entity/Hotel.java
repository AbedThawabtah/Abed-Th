package com.tourism.booking.catalog.entity;

import com.tourism.booking.security.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "hotels",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "city"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(length = 500)
    private String address;

    private Integer starRating;

    @Column(length = 2000)
    private String description;

    @Column(name = "image_path", length = 500)
    private String imagePath;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RoomType> roomTypes = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "hotel_owners",
            joinColumns = @JoinColumn(name = "hotel_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private List<AppUser> owners = new ArrayList<>();
}


//Builder هو نمط تصميم (Design Pattern) يساعدك على إنشاء كائن (Object) فيه حقول كثيرة بطريقة مرتبة وواضحة بدل استخدام Constructor طويل ومعقد.


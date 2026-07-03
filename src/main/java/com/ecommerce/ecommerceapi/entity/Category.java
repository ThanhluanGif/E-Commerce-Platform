package com.ecommerce.ecommerceapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;


@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder  // Hỗ trợ khởi tạo đối tượng nhanh ( Design Pattern Builder)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // --- CẤU HÌNH MỐI QUAN HỆ ĐỆ QUY (SELF-REFERENCE) ---

    // 1. Nhiều danh mục con trỏ về 1 danh mục cha (parent_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    // 2. Một danh mục cha có thể có danh sách nhiều danh mục con bên trong
    // Khai báo này giúp sau này bro gọi lấy menu đa cấp cực kỳ tiện
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> children;
}

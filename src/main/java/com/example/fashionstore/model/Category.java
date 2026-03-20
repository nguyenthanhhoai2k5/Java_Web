package com.example.fashionstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories")
@Data // Tự động tạo Getter, Setter, toString...
@NoArgsConstructor // Tạo constructor không tham số
@AllArgsConstructor // Tạo constructor có đầy đủ tham số
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;
}
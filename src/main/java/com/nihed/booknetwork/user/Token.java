package com.nihed.booknetwork.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Token {

    @Id
    @GeneratedValue
    private Integer id;

    private String token;
    private LocalDate createdAt;
    private LocalDate expiredAt;
    private LocalDate validatedAt;

    @ManyToOne
    @JoinColumn(name="userId", nullable = false)
    private User user;

}

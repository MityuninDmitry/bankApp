package ru.mityunin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notification", schema = "notifications")
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "local_date_time")
    private LocalDateTime localDateTime;

    @Column(name = "message")
    private String message;

    @Column(name = "login")
    private String login;

    @Column(name = "used")
    private Boolean used;

}

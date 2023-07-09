package com.kobi.smartbot.repository.entity;


import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "friend_request_counters")
public class FriendRequestCounterJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_date")
    private LocalDate requestDate;

    @Column(name = "requests_sent")
    private int requestsSent;

    public FriendRequestCounterJpa() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public int getRequestsSent() {
        return requestsSent;
    }

    public void setRequestsSent(int requestsSent) {
        this.requestsSent = requestsSent;
    }
}
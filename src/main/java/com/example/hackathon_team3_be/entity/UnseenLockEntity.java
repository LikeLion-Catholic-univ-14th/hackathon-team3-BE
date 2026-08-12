package com.example.hackathon_team3_be.entity;

import jakarta.persistence.*;

@Entity
@Table(name="unseen_locks", uniqueConstraints=@UniqueConstraint(columnNames={"unseen_id","feature"}))
public class UnseenLockEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="unseen_id", nullable=false) private String unseenId;
    @Column(nullable=false) private String feature;
    protected UnseenLockEntity() { }
    public UnseenLockEntity(String unseenId,String feature){this.unseenId=unseenId;this.feature=feature;}
    public String getFeature(){return feature;}
}

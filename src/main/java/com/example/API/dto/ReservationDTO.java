package com.example.API.dto;

// Wire-format shape kept identical to the old (pre-relations) UserReservations
// entity: programid + userid stay flat, matching the pre-migration frontend contract.
public class ReservationDTO {
    private Long id;
    private Long programid;
    private Long userid;

    public ReservationDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProgramid() {
        return programid;
    }

    public void setProgramid(Long programid) {
        this.programid = programid;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }
}

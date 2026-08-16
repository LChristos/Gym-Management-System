package com.example.API.customrepositories;

import com.example.API.models.Programs;
import com.example.API.models.UserReservations;
import java.util.*;
public interface ReservationsRepositoryCustom {
    List<UserReservations> findReservationsByUserid(String id);
    public UserReservations findByUseridAndProgramid(String userid , String programid);

}

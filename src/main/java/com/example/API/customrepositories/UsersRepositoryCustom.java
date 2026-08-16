package com.example.API.customrepositories;

import com.example.API.models.Programs;
import com.example.API.models.UserReservations;
import com.example.API.models.Users;
import java.util.*;



public interface  UsersRepositoryCustom {
    List<Users> GetNotAccepted();

    List<Users> GetAllAccepted();

    List<UserReservations> findByUserid(String id);
}

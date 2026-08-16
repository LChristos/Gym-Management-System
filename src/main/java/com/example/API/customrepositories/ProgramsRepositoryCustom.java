package com.example.API.customrepositories;

import com.example.API.models.Programs;
import java.util.*;

public interface ProgramsRepositoryCustom {
    List<Programs> FindAllPrograms();

    List<Programs> ShowAllAvailable();

    List<Programs> showProgram(String service_name);
}

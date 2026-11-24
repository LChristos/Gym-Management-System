package com.example.API.service;

import com.example.API.Repositories.ProgramsRepository;
import com.example.API.Repositories.ReservationsRepository;
import com.example.API.Repositories.ServicesRepository;
import com.example.API.models.Services;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ServiceManagementService {

    private final ServicesRepository servicesRepository;
    private final ReservationsRepository reservationsRepository;
    private final ProgramsRepository programsRepository;

    public ServiceManagementService(ServicesRepository servicesRepository, ReservationsRepository reservationsRepository, ProgramsRepository programsRepository){
        this.programsRepository = programsRepository;
        this.reservationsRepository = reservationsRepository;
        this.servicesRepository= servicesRepository;
    }

    @Transactional
    public void deleteServiceData(Long id){
        Services service = servicesRepository.findByIdForUpdate(id).orElseThrow(
                () -> new NoSuchElementException("Service not found!"));
        LocalDate date = LocalDate.now();
        List<Long> program_ids = programsRepository.findByServiceIdAndProgramdateGreaterThanEqual(date, id);
        if (!program_ids.isEmpty()){
            reservationsRepository.deleteAllReservationsFromPrograms(program_ids);
        }
        programsRepository.deleteByService_IdFuturePrograms(date ,id);
        programsRepository.detachServiceFromProgram(id);
        servicesRepository.deleteById(id);

    }
}

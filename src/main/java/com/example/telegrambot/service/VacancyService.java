package com.example.telegrambot.service;


import com.example.telegrambot.dto.VacancyDto;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO add parsing web site
@Service
public class VacancyService {
    private final Map<String, VacancyDto> vacancies = new HashMap<>();

    @Autowired
    private VacancyReaderService vacancyReaderService;

    @PostConstruct
    public void init(){
        List<VacancyDto> list = vacancyReaderService.getVacanciesFromFile("vacancies.csv");
        for (VacancyDto vacancy: list){
            vacancies.put(vacancy.getId(), vacancy);
        }
//        VacancyDto juniorMaDeveloper = new VacancyDto();
//        juniorMaDeveloper.setId("1");
//        juniorMaDeveloper.setTitle("Junior Dev at MA");
//        juniorMaDeveloper.setShortDescription("J ma");
//        vacancies.put("1", juniorMaDeveloper);
//
//        VacancyDto juniorGoogleDeveloper = new VacancyDto();
//        juniorGoogleDeveloper.setId("4");
//        juniorGoogleDeveloper.setTitle("Junior Dev at Google");
//        juniorGoogleDeveloper.setShortDescription("J Google");
//        vacancies.put("4", juniorGoogleDeveloper);
//
//
//
//        VacancyDto middleMaDeveloper = new VacancyDto();
//        middleMaDeveloper.setId("2");
//        middleMaDeveloper.setTitle("Middle Dev at MA");
//        middleMaDeveloper.setShortDescription("M ma");
//        vacancies.put("2", middleMaDeveloper);
//
//        VacancyDto seniorMaDeveloper = new VacancyDto();
//        seniorMaDeveloper.setId("3");
//        seniorMaDeveloper.setTitle("Senior Dev at MA");
//        seniorMaDeveloper.setShortDescription("S ma");
//        vacancies.put("3", seniorMaDeveloper);
    }

    public List<VacancyDto> getJuniorVacancies(){
        return vacancies.values()
                .stream()
                .filter(v -> v.getTitle()
                        .toLowerCase()
                        .contains("junior"))
                .toList();
    }
    public List<VacancyDto> getMiddleVacancies(){
        return vacancies.values()
                .stream()
                .filter(v -> v.getTitle()
                        .toLowerCase()
                        .contains("middle"))
                .toList();
    }
    public List<VacancyDto> getSeniorVacancies(){
        return vacancies.values()
                .stream()
                .filter(v -> v.getTitle()
                        .toLowerCase()
                        .contains("senior"))
                .toList();
    }

    public VacancyDto get(String id){
        return vacancies.get(id);
    }

    //TODO добавить вывод для всех остальных вакансий

}

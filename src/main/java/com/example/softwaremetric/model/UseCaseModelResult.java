package com.example.softwaremetric.model;

import java.util.List;

public record UseCaseModelResult(
        List<UseCaseModelItem> actors,
        List<UseCaseModelItem> useCases,
        int simpleActorCount,
        int averageActorCount,
        int complexActorCount,
        int simpleUseCaseCount,
        int averageUseCaseCount,
        int complexUseCaseCount,
        int actorWeight,
        int useCaseWeight,
        int unadjustedUseCasePoints,
        double useCasePoints,
        double effortHours,
        double effortPersonMonths
) {
}

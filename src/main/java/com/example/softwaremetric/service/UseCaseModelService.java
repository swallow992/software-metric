package com.example.softwaremetric.service;

import com.example.softwaremetric.model.UseCaseModelInput;
import com.example.softwaremetric.model.UseCaseModelItem;
import com.example.softwaremetric.model.UseCaseModelResult;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class UseCaseModelService {

    public UseCaseModelResult calculate(UseCaseModelInput input) {
        validate(input);

        List<UseCaseModelItem> actors = parseItems(input.actorsText(), ItemType.ACTOR);
        List<UseCaseModelItem> useCases = parseItems(input.useCasesText(), ItemType.USE_CASE);

        int simpleActorCount = count(actors, Complexity.SIMPLE);
        int averageActorCount = count(actors, Complexity.AVERAGE);
        int complexActorCount = count(actors, Complexity.COMPLEX);
        int simpleUseCaseCount = count(useCases, Complexity.SIMPLE);
        int averageUseCaseCount = count(useCases, Complexity.AVERAGE);
        int complexUseCaseCount = count(useCases, Complexity.COMPLEX);
        int actorWeight = actors.stream().mapToInt(UseCaseModelItem::weight).sum();
        int useCaseWeight = useCases.stream().mapToInt(UseCaseModelItem::weight).sum();
        int unadjustedUseCasePoints = actorWeight + useCaseWeight;
        double useCasePoints = unadjustedUseCasePoints * input.technicalFactor() * input.environmentalFactor();
        double effortHours = useCasePoints * input.hoursPerUseCasePoint();
        double effortPersonMonths = effortHours / 160.0;

        return new UseCaseModelResult(
                actors,
                useCases,
                simpleActorCount,
                averageActorCount,
                complexActorCount,
                simpleUseCaseCount,
                averageUseCaseCount,
                complexUseCaseCount,
                actorWeight,
                useCaseWeight,
                unadjustedUseCasePoints,
                useCasePoints,
                effortHours,
                effortPersonMonths
        );
    }

    private void validate(UseCaseModelInput input) {
        if (input == null) {
            throw new IllegalArgumentException("用例模型输入不能为空。");
        }
        if (input.technicalFactor() <= 0 || input.environmentalFactor() <= 0) {
            throw new IllegalArgumentException("技术因子和环境因子必须大于 0。");
        }
        if (input.hoursPerUseCasePoint() <= 0) {
            throw new IllegalArgumentException("每用例点工时必须大于 0。");
        }
    }

    private List<UseCaseModelItem> parseItems(String text, ItemType itemType) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(itemType.emptyMessage);
        }

        List<UseCaseModelItem> items = Arrays.stream(text.split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .map(line -> parseLine(line, itemType))
                .toList();

        if (items.isEmpty()) {
            throw new IllegalArgumentException(itemType.emptyMessage);
        }
        return items;
    }

    private UseCaseModelItem parseLine(String line, ItemType itemType) {
        String[] parts = line.split("\\s*[,，|]\\s*");
        if (parts.length < 2) {
            parts = line.trim().split("\\s+");
        }
        if (parts.length < 2) {
            throw new IllegalArgumentException("无法解析结构化输入：" + line);
        }

        String complexityText = parts[parts.length - 1].trim();
        String name = line.substring(0, line.lastIndexOf(complexityText)).replaceAll("[,，|\\s]+$", "").trim();
        if (name.isBlank()) {
            throw new IllegalArgumentException("名称不能为空：" + line);
        }

        Complexity complexity = Complexity.from(complexityText);
        return new UseCaseModelItem(name, complexity.displayName, itemType.weight(complexity));
    }

    private int count(List<UseCaseModelItem> items, Complexity complexity) {
        return (int) items.stream()
                .filter(item -> complexity.displayName.equals(item.complexity()))
                .count();
    }

    private enum ItemType {
        ACTOR("参与者不能为空。") {
            @Override
            int weight(Complexity complexity) {
                return switch (complexity) {
                    case SIMPLE -> 1;
                    case AVERAGE -> 2;
                    case COMPLEX -> 3;
                };
            }
        },
        USE_CASE("用例不能为空。") {
            @Override
            int weight(Complexity complexity) {
                return switch (complexity) {
                    case SIMPLE -> 5;
                    case AVERAGE -> 10;
                    case COMPLEX -> 15;
                };
            }
        };

        private final String emptyMessage;

        ItemType(String emptyMessage) {
            this.emptyMessage = emptyMessage;
        }

        abstract int weight(Complexity complexity);
    }

    private enum Complexity {
        SIMPLE("简单", "simple", "简单", "低"),
        AVERAGE("一般", "average", "normal", "medium", "一般", "普通", "中"),
        COMPLEX("复杂", "complex", "复杂", "高");

        private final String displayName;
        private final List<String> aliases;

        Complexity(String displayName, String... aliases) {
            this.displayName = displayName;
            this.aliases = List.of(aliases);
        }

        private static Complexity from(String value) {
            String normalized = value.toLowerCase(Locale.ROOT);
            for (Complexity complexity : values()) {
                if (complexity.aliases.contains(normalized)) {
                    return complexity;
                }
            }
            throw new IllegalArgumentException("复杂度仅支持：简单、一般、复杂。当前值：" + value);
        }
    }
}

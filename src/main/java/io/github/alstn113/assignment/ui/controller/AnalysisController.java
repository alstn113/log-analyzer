package io.github.alstn113.assignment.ui.controller;

import io.github.alstn113.assignment.application.AnalysisCommandService;
import io.github.alstn113.assignment.application.AnalysisQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AnalysisController implements AnalysisControllerDocs {

    private final AnalysisCommandService analysisCommandService;
    private final AnalysisQueryService analysisQueryService;
}

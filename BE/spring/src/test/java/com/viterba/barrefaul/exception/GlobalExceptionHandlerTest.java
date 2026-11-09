package com.viterba.barrefaul.exception;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.viterba.barrefaul.api.FreestylerController;
import com.viterba.barrefaul.dto.TournamentResultsRequest;
import com.viterba.barrefaul.service.FreestylerService;

// L'handler è globale: lo si prova attraverso FreestylerController, che ha body validato, id nel path e un 400 di business
@WebMvcTest(FreestylerController.class)
class GlobalExceptionHandlerTest {

    private static final String RESULTS_URL = "/api/freestylers/tournament-results";
    private static final String MALFORMED_MESSAGE = "Malformed JSON request or invalid value";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FreestylerService freestylerService;

    @Test
    void notFound_returns404WithErrorBody() throws Exception {
        when(freestylerService.findById(99L)).thenThrow(new ResourceNotFoundException("Freestyler", 99L));

        mockMvc.perform(get("/api/freestylers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Freestyler not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists())
                // errors è null e Jackson non serializza i null
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void validationFailure_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/freestylers").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                // Il testo dell'errore dipende dalla lingua del server: si controlla solo il campo
                .andExpect(jsonPath("$.errors.alias").isNotEmpty());

        verifyNoInteractions(freestylerService);
    }

    @Test
    void validationFailure_emptyEntriesReturns400() throws Exception {
        mockMvc.perform(post(RESULTS_URL).contentType(MediaType.APPLICATION_JSON).content("""
                {"entries": []}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.entries").isNotEmpty());

        verifyNoInteractions(freestylerService);
    }

    @Test
    void validationFailure_nestedFieldIsReportedWithItsPath() throws Exception {
        mockMvc.perform(post(RESULTS_URL).contentType(MediaType.APPLICATION_JSON).content("""
                {"entries": [{"placement": "FIRST_PLACE"}]}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['entries[0].freestylerId']").isNotEmpty());

        verifyNoInteractions(freestylerService);
    }

    @Test
    void invalidRequest_returns400WithServiceMessage() throws Exception {
        doThrow(new InvalidRequestException("Freestyler 1 appears more than once"))
                .when(freestylerService).registerResults(any(TournamentResultsRequest.class));

        mockMvc.perform(post(RESULTS_URL).contentType(MediaType.APPLICATION_JSON).content("""
                {"entries": [{"freestylerId": 1, "placement": "FIRST_PLACE"}]}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Freestyler 1 appears more than once"));
    }

    @Test
    void malformedJson_returns400WithGenericMessage() throws Exception {
        mockMvc.perform(post("/api/freestylers").contentType(MediaType.APPLICATION_JSON).content("{\"alias\":"))
                .andExpect(status().isBadRequest())
                // Il messaggio dell'eccezione originale non deve arrivare al client
                .andExpect(jsonPath("$.message").value(MALFORMED_MESSAGE));

        verifyNoInteractions(freestylerService);
    }

    @Test
    void unknownEnumValue_returns400WithGenericMessage() throws Exception {
        mockMvc.perform(post(RESULTS_URL).contentType(MediaType.APPLICATION_JSON).content("""
                {"entries": [{"freestylerId": 1, "placement": "FOURTH_PLACE"}]}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(MALFORMED_MESSAGE));

        verifyNoInteractions(freestylerService);
    }

    @Test
    void missingBody_returns400() throws Exception {
        mockMvc.perform(post("/api/freestylers").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(MALFORMED_MESSAGE));

        verifyNoInteractions(freestylerService);
    }

    @Test
    void pathVariableOfWrongType_returns400NamingTheParameter() throws Exception {
        mockMvc.perform(get("/api/freestylers/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for parameter: id"));

        verifyNoInteractions(freestylerService);
    }

}
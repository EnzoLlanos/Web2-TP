package com.example.demo.controller;

import com.example.demo.AbstractIntegrationTest;
import com.example.demo.dto.auth.LoginRequestDTO;
import com.example.demo.dto.auth.RegistroRequestDTO;
import com.example.demo.dto.lista.ListaRequestDTO;
import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración HTTP + seguridad + PostgreSQL real (Testcontainers).
 * Flyway corre V1..V6 contra una base limpia al levantar el contexto.
 */
class ListaControllerIT extends AbstractIntegrationTest {

    private static final String PASSWORD = "secreta123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void limpiarBase() {
        jdbcTemplate.update("DELETE FROM favoritos");
        jdbcTemplate.update("DELETE FROM listas");
        jdbcTemplate.update("DELETE FROM usuarios");
    }

    @Test
    void login_credencialesValidas_retorna200ConToken() throws Exception {
        String email = emailUnico();
        registrar(email);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginRequestDTO(email, PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"));
    }

    @Test
    void login_credencialesInvalidas_retorna401() throws Exception {
        String email = emailUnico();
        registrar(email);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginRequestDTO(email, "password-incorrecta"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void obtener_listaPropia_retorna200() throws Exception {
        String token = tokenDe(emailUnico());
        long listaId = crearLista(token, "Mia");

        mockMvc.perform(get("/api/listas/{id}", listaId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listaId))
                .andExpect(jsonPath("$.nombre").value("Mia"));
    }

    @Test
    void obtener_listaAjena_retorna403() throws Exception {
        String tokenAna = tokenDe(emailUnico());
        String tokenBeto = tokenDe(emailUnico());
        long listaDeBeto = crearLista(tokenBeto, "De Beto");

        mockMvc.perform(get("/api/listas/{id}", listaDeBeto)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAna))
                .andExpect(status().isForbidden());
    }

    @Test
    void listar_sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/listas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void admin_obtieneListaAjena_retorna200() throws Exception {
        String tokenDuenio = tokenDe(emailUnico());
        long listaAjena = crearLista(tokenDuenio, "De otro usuario");

        String emailAdmin = emailUnico();
        usuarios.save(new Usuario(null, emailAdmin,
                passwordEncoder.encode(PASSWORD), Rol.ADMIN));
        String tokenAdmin = login(emailAdmin);

        mockMvc.perform(get("/api/listas/{id}", listaAjena)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listaAjena));
    }

    private void registrar(String email) throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RegistroRequestDTO(email, PASSWORD))))
                .andExpect(status().isCreated());
    }

    private String tokenDe(String email) throws Exception {
        registrar(email);
        return login(email);
    }

    private String login(String email) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginRequestDTO(email, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return jsonMapper.readTree(body).get("token").asText();
    }

    private long crearLista(String token, String nombre) throws Exception {
        String body = mockMvc.perform(post("/api/listas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ListaRequestDTO(nombre))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode respuesta = jsonMapper.readTree(body);
        return respuesta.get("id").asLong();
    }

    private String emailUnico() {
        return "user-" + UUID.randomUUID() + "@demo.com";
    }

    private String json(Object valor) throws Exception {
        return jsonMapper.writeValueAsString(valor);
    }
}

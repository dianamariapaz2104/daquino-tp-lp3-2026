package py.edu.uc.lp3.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import py.edu.uc.lp3.domain.RechazoCompaneroException;

/**
 * Cliente HTTP de la capa Service para consumir los servicios externos de los compañeros A2 y A3
 * según sus especificaciones OpenAPI 3, además de la API de práctica vista en clase.
 * Registra cada paso de la integración en nivel DEBUG.
 */
@Service
public class CompaneroApiClient {

    private static final Logger log = LoggerFactory.getLogger(CompaneroApiClient.class);

    private final RestClient restClient;
    private final Map<String, Map<String, Object>> mapaCacheIntegracion = new ConcurrentHashMap<>();

    @Value("${cs2.peers.a2.nombre:Matias Enciso (A2)}")
    private String a2Nombre;

    @Value("${cs2.peers.a2.base-url:http://localhost:8081}")
    private String a2BaseUrl;

    @Value("${cs2.peers.a2.endpoint:/api/armas/1/disparar}")
    private String a2Endpoint;

    @Value("${cs2.peers.a2.openapi-url:http://localhost:8081/v3/api-docs}")
    private String a2OpenApiUrl;

    @Value("${cs2.peers.a3.nombre:Compañero CS2 (A3)}")
    private String a3Nombre;

    @Value("${cs2.peers.a3.base-url:http://localhost:8082}")
    private String a3BaseUrl;

    @Value("${cs2.peers.a3.endpoint:/api/simulacion/explosion}")
    private String a3Endpoint;

    @Value("${cs2.peers.a3.openapi-url:http://localhost:8082/v3/api-docs}")
    private String a3OpenApiUrl;

    @Value("${cs2.external.restful-api-url:https://api.restful-api.dev/objects}")
    private String externalRestfulApiUrl;

    public CompaneroApiClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    /**
     * Consulta la Regla 1 en el servicio del compañero A2 (ej. validación de disparo y consumo de munición).
     */
    public Map<String, Object> validarReglaEnA2(int balasDisparo, String equipoObjetivo, int distanciaMetros) {
        String url = UriComponentsBuilder.fromHttpUrl(a2BaseUrl + a2Endpoint)
                .queryParam("balas", balasDisparo)
                .queryParam("equipoObjetivo", equipoObjetivo)
                .queryParam("distancia", distanciaMetros)
                .toUriString();

        log.debug("[POO-09][A1 -> A2] Iniciando llamada HTTP POST a compañero A2 ({}) en URL: {}", a2Nombre, url);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("balas", balasDisparo);
        payload.put("equipoObjetivo", equipoObjetivo);
        payload.put("distanciaMetros", distanciaMetros);

        try {
            String respuestaRaw = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            log.debug("[POO-09][A2 -> A1] Respuesta exitosa (2xx) recibida de A2 ({}): {}", a2Nombre, respuestaRaw);

            Map<String, Object> resultado = new LinkedHashMap<>();
            resultado.put("companero", "A2");
            resultado.put("nombre", a2Nombre);
            resultado.put("urlConsultada", url);
            resultado.put("estado", "ACEPTADO");
            resultado.put("respuestaRemota", respuestaRaw);
            mapaCacheIntegracion.put("ultima_respuesta_A2", resultado);
            return resultado;
        } catch (RestClientResponseException ex) {
            String cuerpoError = ex.getResponseBodyAsString();
            log.debug("[POO-09][A2 -> A1] RECHAZO recibido de A2 ({}) con código HTTP {}: {}",
                    a2Nombre, ex.getStatusCode().value(), cuerpoError);
            throw new RechazoCompaneroException(
                    "A2",
                    a2Nombre,
                    url,
                    ex.getStatusCode().value(),
                    cuerpoError,
                    "La API del compañero A2 (" + a2Nombre + ") rechazó la jugada con HTTP "
                            + ex.getStatusCode().value() + ": " + cuerpoError
            );
        } catch (RestClientException ex) {
            log.debug("[POO-09][A2 -> A1] Error de conexión al consultar A2 ({}) en {}: {}", a2Nombre, url, ex.getMessage());
            throw new RechazoCompaneroException(
                    "A2",
                    a2Nombre,
                    url,
                    503,
                    ex.getMessage(),
                    "No se pudo conectar con la API del compañero A2 (" + a2Nombre + ") en " + url + ": " + ex.getMessage()
            );
        }
    }

    /**
     * Consulta la Regla 2 en el servicio del compañero A3 (ej. validación de explosión, mitigación de blindaje y fuego amigo).
     */
    public Map<String, Object> validarReglaEnA3(Long armaIdRemoto, Long jugadorIdRemoto, double distanciaMetros, String equipoObjetivo) {
        String url = UriComponentsBuilder.fromHttpUrl(a3BaseUrl + a3Endpoint)
                .queryParam("distancia", distanciaMetros)
                .queryParam("equipoObjetivo", equipoObjetivo)
                .toUriString();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("armaId", armaIdRemoto != null ? armaIdRemoto : 3L);
        payload.put("jugadorId", jugadorIdRemoto != null ? jugadorIdRemoto : 1L);
        payload.put("distanciaMetros", distanciaMetros);
        payload.put("equipoObjetivo", equipoObjetivo);

        log.debug("[POO-09][A1 -> A3] Iniciando llamada HTTP POST a compañero A3 ({}) en URL: {} con payload: {}",
                a3Nombre, url, payload);

        try {
            String respuestaRaw = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            log.debug("[POO-09][A3 -> A1] Respuesta exitosa (2xx) recibida de A3 ({}): {}", a3Nombre, respuestaRaw);

            Map<String, Object> resultado = new LinkedHashMap<>();
            resultado.put("companero", "A3");
            resultado.put("nombre", a3Nombre);
            resultado.put("urlConsultada", url);
            resultado.put("estado", "ACEPTADO");
            resultado.put("respuestaRemota", respuestaRaw);
            mapaCacheIntegracion.put("ultima_respuesta_A3", resultado);
            return resultado;
        } catch (RestClientResponseException ex) {
            String cuerpoError = ex.getResponseBodyAsString();
            log.debug("[POO-09][A3 -> A1] RECHAZO recibido de A3 ({}) con código HTTP {}: {}",
                    a3Nombre, ex.getStatusCode().value(), cuerpoError);
            throw new RechazoCompaneroException(
                    "A3",
                    a3Nombre,
                    url,
                    ex.getStatusCode().value(),
                    cuerpoError,
                    "La API del compañero A3 (" + a3Nombre + ") rechazó la jugada con HTTP "
                            + ex.getStatusCode().value() + ": " + cuerpoError
            );
        } catch (RestClientException ex) {
            log.debug("[POO-09][A3 -> A1] Error de conexión al consultar A3 ({}) en {}: {}", a3Nombre, url, ex.getMessage());
            throw new RechazoCompaneroException(
                    "A3",
                    a3Nombre,
                    url,
                    503,
                    ex.getMessage(),
                    "No se pudo conectar con la API del compañero A3 (" + a3Nombre + ") en " + url + ": " + ex.getMessage()
            );
        }
    }

    /**
     * Práctica de clase: consulta la API externa https://api.restful-api.dev/objects con el cliente HTTP de Spring Boot
     * y guarda el resultado en el Map en memoria respetando la arquitectura en capas.
     */
    public List<Map<String, Object>> consultarApiExternaRestfulDev() {
        log.debug("[POO-09][Cliente Externo] Consultando API externa en: {}", externalRestfulApiUrl);
        List<Map<String, Object>> objetos = restClient.get()
                .uri(externalRestfulApiUrl)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

        if (objetos != null) {
            for (Map<String, Object> obj : objetos) {
                Object id = obj.get("id");
                if (id != null) {
                    mapaCacheIntegracion.put("restful_obj_" + id, obj);
                }
            }
        }
        return objetos != null ? objetos : List.of();
    }

    public Map<String, Object> guardarEnMapaData(String clave, Map<String, Object> datos) {
        if (clave == null || clave.trim().isEmpty()) {
            throw new IllegalArgumentException("La clave para guardar en el mapa no puede ser nula ni vacía.");
        }
        if (datos == null || datos.isEmpty()) {
            throw new IllegalArgumentException("Los datos para guardar en el mapa no pueden estar vacíos.");
        }
        log.debug("[POO-09][Mapa Data] Guardando entrada con clave '{}': {}", clave, datos);
        mapaCacheIntegracion.put(clave.trim(), new LinkedHashMap<>(datos));
        return mapaCacheIntegracion.get(clave.trim());
    }

    public Map<String, Map<String, Object>> obtenerMapaDataCompleto() {
        return new LinkedHashMap<>(mapaCacheIntegracion);
    }

    public Map<String, Object> obtenerConfiguracionGrupo() {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("A1_propio", Map.of(
                "nombre", "Diana Aquino (A1)",
                "openApiJson", "/v3/api-docs",
                "swaggerUi", "/swagger-ui/index.html"
        ));
        mapa.put("A2_companero", Map.of(
                "nombre", a2Nombre,
                "baseUrl", a2BaseUrl,
                "endpointRegla", a2Endpoint,
                "openApiUrl", a2OpenApiUrl
        ));
        mapa.put("A3_companero", Map.of(
                "nombre", a3Nombre,
                "baseUrl", a3BaseUrl,
                "endpointRegla", a3Endpoint,
                "openApiUrl", a3OpenApiUrl
        ));
        return mapa;
    }

    public void configurarUrlsParaTesting(String nuevaA2BaseUrl, String nuevoA2Endpoint, String nuevaA3BaseUrl, String nuevoA3Endpoint) {
        this.a2BaseUrl = nuevaA2BaseUrl;
        this.a2Endpoint = nuevoA2Endpoint;
        this.a3BaseUrl = nuevaA3BaseUrl;
        this.a3Endpoint = nuevoA3Endpoint;
    }
}

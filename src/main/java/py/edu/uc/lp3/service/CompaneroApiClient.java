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

    @Value("${cs2.peers.a2.endpoint:/api/simulacion/explosion}")
    private String a2Endpoint;

    @Value("${cs2.peers.a2.openapi-url:http://localhost:8081/v3/api-docs}")
    private String a2OpenApiUrl;

    @Value("${cs2.peers.a3.nombre:Fabricio Gonzalez (A3)}")
    private String a3Nombre;

    @Value("${cs2.peers.a3.base-url:http://localhost:8082}")
    private String a3BaseUrl;

    @Value("${cs2.peers.a3.endpoint:/api/reglas/danio}")
    private String a3Endpoint;

    @Value("${cs2.peers.a3.openapi-url:http://localhost:8082/v3/api-docs}")
    private String a3OpenApiUrl;

    @Value("${cs2.external.restful-api-url:https://api.restful-api.dev/objects}")
    private String externalRestfulApiUrl;

    public CompaneroApiClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    /**
     * Consulta la Regla en el servicio del compañero A2 (Matias Enciso):
     * GET /api/simulacion/explosion?distancia=2.5&conBlindaje=true&fuegoAmigo=false
     * (y también pasa ?balas=... por si A2_ENDPOINT apunta a /api/armas/1/disparar).
     */
    public Map<String, Object> validarReglaEnA2(int cargadorDisponible, int balasDisparo, String equipoObjetivo, double distanciaMetros) {
        boolean fuegoAmigo = "Counter-Terrorista".equalsIgnoreCase(equipoObjetivo)
                || "CT".equalsIgnoreCase(equipoObjetivo)
                || "COUNTER_TERRORISTA".equalsIgnoreCase(equipoObjetivo);

        // Si balasDisparo <= 0 y estamos llamando a /api/simulacion/explosion, pasamos distancia negativa para provocar el 400 ValidationException en A2
        double distanciaEfectivaA2 = (balasDisparo <= 0 && distanciaMetros >= 0) ? -Math.abs(balasDisparo) : distanciaMetros;

        String url = UriComponentsBuilder.fromHttpUrl(a2BaseUrl + a2Endpoint)
                .queryParam("distancia", distanciaEfectivaA2)
                .queryParam("conBlindaje", true)
                .queryParam("fuegoAmigo", fuegoAmigo)
                .queryParam("balas", balasDisparo)
                .queryParam("equipoObjetivo", equipoObjetivo)
                .toUriString();

        log.debug("[POO-09][A1 -> A2] Iniciando llamada HTTP GET a compañero A2 ({}) en URL: {}",
                a2Nombre, url);

        try {
            String respuestaRaw = restClient.get()
                    .uri(url)
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
     * Consulta la Regla 2 en el servicio del compañero A3:
     * POST /api/reglas/danio con cuerpo {"vida": 100, "armadura": 100, "danio": 36}.
     * Responde 200 con la vida y el chaleco que quedan, o 422 JUGADOR_ELIMINADO si la vida ya es 0.
     * También incluye {"cargador": ..., "disparos": ...} por si se consulta /api/reglas/municion (409 SIN_MUNICION).
     */
    public Map<String, Object> validarReglaEnA3(int vidaObjetivo, int armaduraObjetivo, int danioCalculado, int cargador, int disparos, double distanciaMetros, String equipoObjetivo) {
        String url = a3BaseUrl + a3Endpoint;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("vida", vidaObjetivo);
        payload.put("armadura", armaduraObjetivo);
        payload.put("danio", danioCalculado);
        payload.put("cargador", cargador);
        payload.put("disparos", disparos);
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

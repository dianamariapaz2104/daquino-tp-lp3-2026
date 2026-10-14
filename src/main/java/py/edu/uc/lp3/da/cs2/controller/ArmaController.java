package py.edu.uc.lp3.da.cs2.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import py.edu.uc.lp3.da.cs2.Arma;
import py.edu.uc.lp3.da.cs2.ConModoDeDisparo;
import py.edu.uc.lp3.da.cs2.DesertEagle;
import py.edu.uc.lp3.da.cs2.Equipo;
import py.edu.uc.lp3.da.cs2.Glock18;
import py.edu.uc.lp3.da.cs2.GranadaHE;
import py.edu.uc.lp3.da.cs2.M4A4;
import py.edu.uc.lp3.da.cs2.MP9;
import py.edu.uc.lp3.da.cs2.Nova;

@RestController
public class ArmaController {

	private static final Map<String, Function<Equipo, Arma>> FABRICA = Map.of(
			"deserteagle", DesertEagle::new,
			"glock18", Glock18::new,
			"m4a4", M4A4::new,
			"nova", Nova::new,
			"mp9", MP9::new,
			"granadahe", GranadaHE::new);

	@GetMapping("/api/armas")
	public Map<String, Object> construirArma(@RequestParam("nombre") String nombre) {
		Arma arma = fabricarArma(nombre);
		Map<String, Object> respuesta = new LinkedHashMap<>();
		respuesta.put("arma", arma.getNombre());
		respuesta.put("municion", arma.getMunicion());
		respuesta.put("comportamientoDeCombate", arma.comportamientoDeCombate());
		return respuesta;
	}

	@GetMapping("/armas/crear")
	public Map<String, Object> crearArma(
			@RequestParam("nombre") String nombre,
			@RequestParam(value = "precio", required = false) Integer precio) {
		Arma arma = fabricarArma(nombre);

		if (precio != null) {
			if (precio < 0) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio no puede ser negativo: " + precio);
			}
			arma.setPrecio(precio);
		}

		Map<String, Object> respuesta = new LinkedHashMap<>();
		respuesta.put("arma", arma.getNombre());
		respuesta.put("precio", arma.getPrecio());
		respuesta.put("municion", arma.getMunicion());
		respuesta.put("comportamientoDeCombate", arma.comportamientoDeCombate());
		return respuesta;
	}

	@GetMapping("/api/modo-disparo")
	public Map<String, Object> describirModoDeDisparo(@RequestParam("nombre") String nombre) {
		Arma arma = fabricarArma(nombre);

		if (!(arma instanceof ConModoDeDisparo conModoDeDisparo)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"El arma no declara modo de disparo: " + nombre);
		}

		Map<String, Object> respuesta = new LinkedHashMap<>();
		respuesta.put("arma", arma.getNombre());
		respuesta.put("descripcionDeDisparo", conModoDeDisparo.descripcionDeDisparo());
		return respuesta;
	}

	private Arma fabricarArma(String nombre) {
		Function<Equipo, Arma> fabrica = FABRICA.get(normalizar(nombre));
		if (fabrica == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arma desconocida: " + nombre);
		}
		return fabrica.apply(new Equipo("Counter-Terrorista"));
	}

	private String normalizar(String nombre) {
		return nombre.replaceAll("[\\s-]", "").toLowerCase();
	}
}
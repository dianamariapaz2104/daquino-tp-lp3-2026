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
			"DesertEagle", DesertEagle::new,
			"Glock18", Glock18::new,
			"M4A4", M4A4::new,
			"Nova", Nova::new,
			"MP9", MP9::new,
			"GranadaHE", GranadaHE::new);

	@GetMapping("/api/armas")
	public Map<String, Object> construirArma(@RequestParam("nombre") String nombre) {
		Function<Equipo, Arma> fabrica = FABRICA.get(nombre);
		if (fabrica == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arma desconocida: " + nombre);
		}

		Arma arma = fabrica.apply(new Equipo("Counter-Terrorista"));
		String comportamiento = arma.comportamientoDeCombate();

		Map<String, Object> respuesta = new LinkedHashMap<>();
		respuesta.put("arma", nombre);
		respuesta.put("municion", arma.getMunicion());
		respuesta.put("comportamientoDeCombate", comportamiento);
		return respuesta;
	}
}
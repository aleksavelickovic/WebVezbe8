package com.ftn.PrviMavenVebProjekat.service.impl;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ftn.PrviMavenVebProjekat.model.ClanskaKarta;
import com.ftn.PrviMavenVebProjekat.model.Knjiga;
import com.ftn.PrviMavenVebProjekat.model.Korisnik;
import com.ftn.PrviMavenVebProjekat.service.ClanskaKartaService;
import com.ftn.PrviMavenVebProjekat.service.KnjigaService;
import com.ftn.PrviMavenVebProjekat.service.KorisnikService;

@Service
@Qualifier("fajloviClanskaKarta")
public class ClanskaKartaServiceImpl implements ClanskaKartaService {
	
	@Value("${clanskeKarte.pathToFile}")
	private String pathToFile;
	
	@Autowired
	private KorisnikService korisnikService;
	
	@Autowired
	private KnjigaService knjigaService;
	
    private Map<Long, ClanskaKarta> readFromFile() {

    	Map<Long, ClanskaKarta> clanskeKarte = new HashMap<>();
    	Long nextId = 1L;
    	
    	try {
			Path path = Paths.get(pathToFile);
			System.out.println(path.toFile().getAbsolutePath());
			List<String> lines = Files.readAllLines(path, Charset.forName("UTF-8"));

			for (String line : lines) {
				line = line.trim();
				if (line.equals("") || line.indexOf('#') == 0)
					continue;
				
				String[] tokens = line.split(";");
				Long id = Long.parseLong(tokens[0]);
				String registarskiBrojClanskeKarte = tokens[1];
				
				Long idKorisnika = Long.parseLong(tokens[2]);
				Korisnik korisnik = korisnikService.findOneById(idKorisnika);
				if (korisnik == null) {
					continue;
				}
					
				ClanskaKarta ck = new ClanskaKarta(id, registarskiBrojClanskeKarte, korisnik);

				if (tokens.length > 3) { // postoje iznajmljene knjige
					String idKnjiga = tokens[3];
					System.out.println(idKnjiga);
					String[] knjigeTokens = idKnjiga.split(",");
					for (String token : knjigeTokens) {
						Knjiga k = knjigaService.findOne(Long.valueOf(token));
						if (k != null) {
							ck.getIznajmljenjeKnjige().add(k);
						}
					}
				} 
				
				clanskeKarte.put(id, ck);

				if(nextId<id)
					nextId=id;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return clanskeKarte;
    }
    
    private Map<Long, ClanskaKarta> saveToFile(Map<Long, ClanskaKarta> clanskeKarte) {
    	
    	Map<Long, ClanskaKarta> ret = new HashMap<>();
    	
    	try {
			Path path = Paths.get(pathToFile);
			System.out.println(path.toFile().getAbsolutePath());
			List<String> lines = new ArrayList<>();
			
			for (ClanskaKarta ck : clanskeKarte.values()) {
				String line = ck.getId() + ";" + ck.getRegistarskiBroj() + ";" + ck.getKorisnik().getId();
				if (ck.getIznajmljenjeKnjige().size() > 0) {
					String knjige = getIds(ck.getIznajmljenjeKnjige());
					line += ';' + knjige;
				}

				lines.add(line);
				ret.put(ck.getId(), ck);
			}
			//pisanje svih redova za clanske karte
			Files.write(path, lines, Charset.forName("UTF-8"));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return ret;
    }
    
    private Long nextId(Map<Long, ClanskaKarta> clanskeKarte) {
    	Long nextId = 1L;
    	for (Long id : clanskeKarte.keySet()) {
    		if(nextId<id)
				nextId=id;
		}
    	return ++nextId;
    }
    
    private String getIds(List<Knjiga> knjige) {
    	List<String> ids = new ArrayList<>();
    	for (Knjiga knjiga : knjige) {
			ids.add(knjiga.getId().toString());
		}
    	
    	StringBuilder sb = new StringBuilder();
        for(String s : ids) {
          sb.append(",").append(s);
        }
        return sb.deleteCharAt(0).toString();
    }

	@Override
	public ClanskaKarta findOne(Long id) {
		Map<Long, ClanskaKarta> clanskeKarte = readFromFile();	
		return clanskeKarte.get(id);
	}

	@Override
	public List<ClanskaKarta> findAll() {
		Map<Long, ClanskaKarta> clanskeKarte = readFromFile();	
		return new ArrayList<ClanskaKarta>(clanskeKarte.values());
	}

	@Override
	public ClanskaKarta save(ClanskaKarta clanskaKarta) {
		Map<Long, ClanskaKarta> clanskeKarte = readFromFile();
		Long nextId = nextId(clanskeKarte); 
		
		//u slučaju da clanska karta nema id
		//tada treba da se dodeli id
		if (clanskaKarta.getId() == null) {
			clanskaKarta.setId(nextId++);
			
		}
		clanskeKarte.put(clanskaKarta.getId(), clanskaKarta);
		saveToFile(clanskeKarte);
		return clanskaKarta;
	}

	@Override
	public ClanskaKarta update(ClanskaKarta clanskaKarta) {
		Map<Long, ClanskaKarta> clanskeKarte = readFromFile();
		
		clanskeKarte.put(clanskaKarta.getId(), clanskaKarta);
		saveToFile(clanskeKarte);
		return clanskaKarta;
	}

	@Override
	public ClanskaKarta delete(Long id) {
		Map<Long, ClanskaKarta> clanskeKarte = readFromFile();
		
		if (!clanskeKarte.containsKey(id)) {
			throw new IllegalArgumentException("tried to remove non existing book card");
		}
		
		ClanskaKarta clanskaKarta = clanskeKarte.get(id);
		if (clanskaKarta != null) {
			clanskeKarte.remove(id);
		}
		saveToFile(clanskeKarte);
		return clanskaKarta;
	}

	@Override
	public ClanskaKarta findOneByRegistarskiBroj(String registarskiBroj) {
		Map<Long, ClanskaKarta> clanskeKarte = readFromFile();
		ClanskaKarta found = null;
		for (ClanskaKarta ck : clanskeKarte.values()) {
			if (ck.getRegistarskiBroj().equals(registarskiBroj)) {
				found = ck;
				break;
			}
		}
		
		return found;
	}

}

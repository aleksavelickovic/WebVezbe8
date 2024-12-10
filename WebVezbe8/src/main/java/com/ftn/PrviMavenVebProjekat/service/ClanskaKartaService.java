package com.ftn.PrviMavenVebProjekat.service;

import java.util.List;

import com.ftn.PrviMavenVebProjekat.model.ClanskaKarta;

public interface ClanskaKartaService {
	ClanskaKarta findOne(Long id); 
	List<ClanskaKarta> findAll(); 
	ClanskaKarta save(ClanskaKarta clanskaKarta); 
	ClanskaKarta update(ClanskaKarta clanskaKarta); 
	ClanskaKarta delete(Long id); 
	ClanskaKarta findOneByRegistarskiBroj(String registarskiBroj);
}

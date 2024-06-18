package com.ivanalvarez.screenmatch.dto;

import com.ivanalvarez.screenmatch.model.Categoria;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public record SerieDTO(Long id,
         String titulo,
         Integer totalDeTemporadas,
         Double evaluacion,
         String poster,
         Categoria genero,
         String actores,
         String sinopsis) {
}

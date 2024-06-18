package com.ivanalvarez.screenmatch.principal;

import com.ivanalvarez.screenmatch.model.*;
import com.ivanalvarez.screenmatch.repository.SerieRepository;
import com.ivanalvarez.screenmatch.service.ConsumoAPI;
import com.ivanalvarez.screenmatch.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner sc = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private final String URL = "http://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=9a61bae7";
    private ConvierteDatos conversor = new ConvierteDatos();
    private List<DatosSerie> datosSeries = new ArrayList<>();
    private SerieRepository repositorio;
    private List<Serie> series;
    private Optional<Serie> serieBuscada;

    public Principal(SerieRepository repository) {
        this.repositorio = repository;
    }

    public void muestraMenu() {

        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1.- Buscar series 
                    2.- Buscar episodios
                    3.- Mostrar series buscadas
                    4.- Buscar serie por titulo  
                    5.- Top 5 mejores series 
                    6.- Buscar series por genero   
                    7.- Filtrar series por temporadas y evaluacion   
                    8.- Buscar episodios por titulo
                    9.- Top 5 episodios de una serie             
                    0.- Salir
                    """;
            System.out.println(menu);
            opcion = sc.nextInt();
            sc.nextLine();

            switch (opcion) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    mostrarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriesPorTitulo();
                    break;
                case 5:
                    buscarTop5Series();
                    break;
                case 6:
                    buscarSeriesPorCategoria();
                    break;
                case 7:
                    filtraSeriesPorTemporadaYEvaluacion();
                    break;
                case 8 :
                    buscarEpisodiosPorTitulo();
                    break;
                case 9:
                    buscarTop5Episodios();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }
    }

    private DatosSerie getDatosSerie() {
        System.out.println("Escribe el nombre de la serie que deseas guardar:");
        var nombreSerie = sc.nextLine();
        var json = consumoAPI.obtenerDatos(URL + nombreSerie.replace(" ", "+") + API_KEY);
        System.out.println(json);
        var datos = conversor.obtenerDatos(json, DatosSerie.class);
        return datos;
    }

    private void buscarEpisodioPorSerie() {
        mostrarSeriesBuscadas();
        System.out.println("Escribe el nombre de la serie de la que quieres ver sus episodios");
        var nombreSerie = sc.nextLine();
        Optional<Serie> serie = series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nombreSerie.toLowerCase()))
                .findFirst();

        if (serie.isPresent()){
            var serieEncontrada = serie.get();
            List<DatosTemporada> temporadas = new ArrayList<>();
            for (int i = 1; i <= serieEncontrada.getTotalDeTemporadas() ; i++) {
                var json = consumoAPI.obtenerDatos(URL + serieEncontrada.getTitulo().replace(" ", "+") + "&Season="+i+ API_KEY);
                var datosTemporadas = conversor.obtenerDatos(json, DatosTemporada.class);
                temporadas.add(datosTemporadas);
            }
            temporadas.forEach(System.out::println);
            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        }
    }

    private void buscarSerieWeb() {
        DatosSerie datos = getDatosSerie();
        //datosSeries.add(datos);
        Serie serie = new Serie(datos);
        repositorio.save(serie);
        System.out.println(datos);
    }

    private void mostrarSeriesBuscadas(){
        series = repositorio.findAll();

        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriesPorTitulo(){
        System.out.println("Escribe el nombre de la serie que desea buscar");
        var nombreSerie = sc.nextLine();
        serieBuscada = repositorio.findByTituloContainsIgnoreCase(nombreSerie);
        if (serieBuscada.isPresent()){
            System.out.println("La serie buscada es: " + serieBuscada.get());
        }else{
            System.out.println("Serie no encontrada");
        }
    }

    private void buscarTop5Series(){
        List<Serie> topSeries = repositorio.findTop5ByOrderByEvaluacionDesc();
        topSeries.forEach(s -> System.out.println("Titulo: " + s.getTitulo() + " ,Evaluacion: "+ s.getEvaluacion()));
    }

    private void buscarSeriesPorCategoria(){
        System.out.println("Escribe el genero de la serie que desea buscar");
        var genero = sc.nextLine();
        var categoria = Categoria.fromEspanol(genero);
        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Las series del genero " + genero + " son:");
        seriesPorCategoria.forEach(System.out::println);
    }

    private void filtraSeriesPorTemporadaYEvaluacion(){
        System.out.println("Filtrar series con un maximo de temporadas de: ");
        var totalTemporadas = sc.nextInt();
        sc.nextLine();
        System.out.println("Con evaluacion minima de: ");
        var evaluacion = sc.nextDouble();
        sc.nextLine();
        //List<Serie> filtroSeries = repositorio.findBytotalDeTemporadasLessThanEqualAndEvaluacionGreaterThanEqual(totalTemporadas, evaluacion);
        List<Serie> filtroSeries = repositorio.seriesPorTemporadaYEvaluacion(totalTemporadas, evaluacion);
        System.out.println("*** Series filtradas ***");
        filtroSeries.forEach(s ->
                System.out.println(s.getTitulo() + "  - evaluacion: " + s.getEvaluacion() + "  - Temporadas: " + s.getTotalDeTemporadas()));
    }

    private void buscarEpisodiosPorTitulo(){
        System.out.println("Ingrese el nombre del episodio que desea buscar");
        var nombreEpisodio = sc.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodiosPorNombre(nombreEpisodio);
        episodiosEncontrados.forEach(e -> System.out.printf("Serie: %s - Temporada %s - Episodio %s - Evaluación %s \n",
                e.getSerie().getTitulo(), e.getTemporada(), e.getTitulo(), e.getEvaluacion()));
    }

    private void buscarTop5Episodios(){
        buscarSeriesPorTitulo();
        if (serieBuscada.isPresent()){
            Serie serie = serieBuscada.get();
            List<Episodio> topEpisodios = repositorio.top5Episodios(serie);
            topEpisodios.forEach(e -> System.out.printf("Serie: %s - Temporada %s - Episodio %s - Evaluación %s \n",
                    e.getSerie().getTitulo(), e.getTemporada(), e.getTitulo()   , e.getEvaluacion()));
        }
    }


    //muestra las temporadas y episodios de la serie

        //Muestra solo el titulo de los episodios
//        for (int i = 0; i < datos.totalDeTemporadas(); i++) {
//            List<DatosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
//            for (int j = 0; j < episodiosTemporada.size(); j++) {
//                System.out.println(episodiosTemporada.get(j).titulo());
//            }
//        }
//        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));
//
//        //Obteniendo una lista de episodios
//        List<DatosEpisodio> datosEpisodios = temporadas.stream()
//                .flatMap(t -> t.episodios().stream())
//                .collect(Collectors.toList());
//
//        //top 5 episodios
//        datosEpisodios.stream()
//                .filter(e -> !e.evaluacion().equalsIgnoreCase("N/A"))
//                .sorted(Comparator.comparing(DatosEpisodio::evaluacion).reversed())
//                .limit(5)
//                .forEach(System.out::println);
//        // Convirtiendo los datos a una lista de tipo Episodio
//        List<Episodio> episodios = temporadas.stream()
//                .flatMap(t -> t.episodios().stream()
//                        .map(d -> new Episodio(t.numero(),d)))
//                .collect(Collectors.toList());
//        episodios.forEach(System.out::println);
//        //Busqueda de episodios a partir de un año
//        System.out.println("A partir de que año desea realizar la busqueda");
//        var fecha = sc.nextInt();
//        sc.nextLine();
//
//        LocalDate fechaBusqueda = LocalDate.of(fecha,1,1);
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        episodios.stream()
//                .filter(e -> e.getFechaDeLanzamiento() != null && e.getFechaDeLanzamiento().isAfter(fechaBusqueda))
//                .forEach(e -> System.out.println(
//                        "Temporada " + e.getTemporada() +
//                                " Episodio " + e.getTitulo() +
//                                " Fecha de Lanzamiento " + e.getFechaDeLanzamiento().format(dtf)
//                ));
//        //Busqueda de episodio por titulo
//        System.out.println("Escriba el titulo del episodio que desea ver");
//        var busquedaTitulo = sc.nextLine();
//        Optional<Episodio> episodioBuscado = episodios.stream()
//                .filter(e -> e.getTitulo().toUpperCase().contains(busquedaTitulo.toUpperCase()))
//                .findFirst();
//
//        if (episodioBuscado.isPresent()){
//            System.out.println("Episodio encontrado");
//            System.out.println("Datos: " + episodioBuscado.get());
//        }else {
//            System.out.println("Episodio no encontrado");
//        }
//
//        Map<Integer, Double> evaluacionesPorTemporada = episodios.stream()
//                .filter(e -> e.getEvaluacion() > 0.0)
//                .collect(Collectors.groupingBy(Episodio::getTemporada,
//                        Collectors.averagingDouble(Episodio::getEvaluacion)));
//        System.out.println(evaluacionesPorTemporada);
//
//        DoubleSummaryStatistics est = episodios.stream()
//                .filter(e -> e.getEvaluacion() > 0.0)
//                .collect(Collectors.summarizingDouble(Episodio::getEvaluacion));
//        System.out.println("Promedio de las evaluaciones: " + est.getAverage());
//        System.out.println("Episodio mejor evaluado: " + est.getMax());
//        System.out.println("Episodio peoer evaluado: " + est.getMin());
}

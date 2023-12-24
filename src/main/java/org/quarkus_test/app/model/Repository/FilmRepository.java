package org.quarkus_test.app.model.Repository;

import com.speedment.jpastreamer.application.JPAStreamer;
import com.speedment.jpastreamer.projection.Projection;
import com.speedment.jpastreamer.streamconfiguration.StreamConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.quarkus_test.app.model.Film;
import org.quarkus_test.app.model.Film$;
import java.util.Optional;
import java.util.stream.Stream;

@ApplicationScoped
public class FilmRepository {

    @Inject
    JPAStreamer jpaStreamer;

    private static final int PAGE_SIZE = 20;

    /**
     *
     * @param filmId
     * @return
     *  Cerca film per Id
     */
    public Optional <Film> getFilm(short filmId){
        return jpaStreamer.stream(Film.class)
                .filter(Film$.filmId.equal(filmId))
                .findFirst();
    }


    /**
     *
     * @param minLength
     * @return
     * Recupera tutti i film aggiornati e i prezzi più recenti/aggiornati
     */
    public Stream<Film> getFilms (short minLength){
        return jpaStreamer.stream(Film.class)
                .filter(Film$.length.greaterThan(minLength))
                .sorted(Film$.length);

    }


    /**
     * @param page
     * @param minLength
     * @return
     */
    public Stream<Film> paged (long page, short minLength){
        return jpaStreamer.stream(Projection.select(Film$.filmId, Film$.title, Film$.length))
                .filter(Film$.length.greaterThan(minLength))
                .sorted(Film$.length)
                .skip(page * PAGE_SIZE)
                .limit(PAGE_SIZE);
    }

    /**
     *
     * @param startsWith
     * @param minLength
     * @return
     *  Questo stream consente di visualizzare tutti i film che hanno una minLength decisa
     *  dall' utente e iniziano con una lettera a scelta
     */
    public Stream<Film> actors(String startsWith, short minLength){
        final StreamConfiguration <Film> sc = StreamConfiguration.of(Film.class).joining(Film$.actors);
        return jpaStreamer.stream(sc)
                .filter(Film$.title.startsWith(startsWith).and(Film$.length.greaterThan(minLength)))
                .sorted(Film$.length.reversed());
    }

    /**
     *
     *  Filtrare i film che si vogliono aggiornare
     *  E aggiornare il costo del noleggio dei film
     *
     */
    @Transactional
    public void updateRentalRate(short minLength, Float rentalRate){
        jpaStreamer.stream(Film.class)
                .filter(Film$.length.greaterThan(minLength))
                // Usa un for each per settare un rentalrate per ogni film
                // nella pipeline
                .forEach(f -> {
                    f.setRentalRate(rentalRate);
                });
    }







}

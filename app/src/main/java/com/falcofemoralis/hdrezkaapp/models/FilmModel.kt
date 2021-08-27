package com.falcofemoralis.hdrezkaapp.models

import android.util.ArrayMap
import android.util.Log
import android.webkit.CookieManager
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.objects.*
import kotlinx.coroutines.*
import org.json.JSONObject
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

object FilmModel {
    private const val FILM_TITLE = "div.b-post__title h1"
    private const val FILM_POSTER = "div.b-sidecover a"
    private const val FILM_TABLE_INFO = "table.b-post__info tbody tr"
    private const val FILM_IMDB_RATING = "span.imdb span"
    private const val FILM_KP_RATING = "span.kp span"
    private const val FILM_WA_RATING = "span.wa span"

    private const val WATCH_ADD = "/engine/ajax/schedule_watched.php"
    private const val RATING_ADD = "/engine/ajax/rating.php"
    private const val GET_FILM_POST = "/engine/ajax/quick_content.php"
    private const val GET_STREAM_POST = "/ajax/get_cdn_series"
    private const val SEND_WATCH = "/ajax/send_save"

    fun getMainData(film: Film): Film {
        try {
            if (film.filmId == null) {
                getMainDataByLink(film)
            } else {
                getMainDataById(film)
            }
        } catch (e: Exception) {
            throw e
        }

        film.hasMainData = true

        return film
    }

    private fun getMainDataById(film: Film): Film {
        val data: ArrayMap<String, String> = ArrayMap()
        data["id"] = film.filmId.toString()
        data["is_touch"] = "1"

        val doc: Document?
        try {
            doc = Jsoup.connect(SettingsData.provider + GET_FILM_POST)
                .data(data)
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .ignoreContentType(true)
                .post()
        } catch (e: Exception) {
            throw e
        }

        if (doc != null) {
            val titleEl = doc.select("div.b-content__bubble_title a")
            film.filmLink = titleEl.attr("href")
            film.type = getTypeByName(doc.select("i.entity").text())
            film.title = titleEl.text()
            film.ratingIMDB = doc.select("span.imdb b").text()
            film.ratingKP = doc.select("span.kp b").text()
            //    film.ratingWA = doc.select("span.wa b").text()
            val genres: ArrayList<String> = ArrayList()
            val genresEls = doc.select("div.b-content__bubble_text a")
            for (el in genresEls) {
                genres.add(el.text())
            }
            film.genres = genres
        } else {
            throw HttpStatusException("failed to get film", 400, SettingsData.provider)
        }

        return film
    }

    private fun getMainDataByLink(film: Film): Film {
        val filmPage: Document = Jsoup.connect(film.filmLink).get()

        film.type = film.filmLink?.split("/")?.get(3)?.let { getTypeByName(it) }
        film.title = filmPage.select(FILM_TITLE).text()

        // Parse info table
        val table: Elements = filmPage.select(FILM_TABLE_INFO)
        for (tr in table) {
            val td: Elements = tr.select("td")
            if (td.size > 0) {
                val h2Els: Elements = td[0].select("h2")
                if (h2Els.size > 0) {
                    val h2: Element = h2Els[0]

                    when (h2.text()) {
                        "Рейтинги" -> {
                            film.ratingIMDB = td[1].select(FILM_IMDB_RATING).text()
                            film.ratingKP = td[1].select(FILM_KP_RATING).text()
                            //  film.ratingWA = td[1].select(FILM_WA_RATING).text()
                        }
                    }
                }
            }
        }

        return film
    }

    private fun getTypeByName(name: String): String {
        return when (name) {
            "series" -> "Сериал"
            "cartoons" -> "Мультфильм"
            "films" -> "Фильм"
            "animation" -> "Аниме"
            else -> name
        }
    }

    fun getAdditionalData(film: Film): Film {
        val document: Document = Jsoup.connect(film.filmLink)
            .header("Cookie", CookieManager.getInstance().getCookie(SettingsData.provider) + "; allowed_comments=1;")
            .get()
        film.origTitle = document.select("div.b-post__origtitle").text()
        film.description = document.select("div.b-post__description_text").text()
        film.votesIMDB = document.select("span.imdb i").text()
        film.votesKP = document.select("span.kp i").text()
        film.votesWA = document.select("span.wa i").text()
        film.runtime = document.select("td[itemprop=duration]").text()
        film.filmId = document.select("div.b-userset__fav_holder").attr("data-post_id").toInt()

        val table: Elements = document.select(FILM_TABLE_INFO)
        // Parse info table
        for (tr in table) {
            val td: Elements = tr.select("td")
            if (td.size > 0) {
                val h2Els: Elements = td[0].select("h2")
                if (h2Els.size > 0) {
                    val h2: Element = h2Els[0]

                    when (h2.text()) {
                        "Рейтинги" -> {
                            film.ratingWA = td[1].select(FILM_WA_RATING).text()
                        }
                        "Дата выхода" -> {
                            film.date = td[1].ownText()
                            film.year = td[1].select("a").text()
                        }
                        "Страна" -> {
                            val countries: ArrayList<String> = ArrayList()
                            for (el in td[1].select("a")) {
                                countries.add(el.text())
                            }
                            film.countries = countries
                        }
                    }
                }
            }
        }

        val hrRatingEl = document.select("div.b-post__rating")
        film.ratingHR = hrRatingEl.select("span.num").text()
        film.votesHR = hrRatingEl.select("span.votes span").text()
        film.isHRratingActive = hrRatingEl.select("div.b-post__rating_wrapper").isNullOrEmpty()

        val posterElement: Element = document.select(FILM_POSTER)[0]
        film.fullSizePosterPath = posterElement.attr("href")
        film.posterPath = posterElement.select("img").attr("src")

        val actors: ArrayList<Actor> = ArrayList()
        val directors: ArrayList<Actor> = ArrayList()
        val personsElements: Elements = document.select("div.persons-list-holder")
        for (el in personsElements) {
            val els: Elements = el.select("span.item")

            if (el.select("span.inline h2").text() == "В ролях актеры") {
                for (actorElement in els) {
                    val pEl = actorElement.select("span.person-name-item")
                    val id: String = pEl.attr("data-id")
                    if (id.isNotEmpty()) {
                        actors.add(Actor(id.toInt(), pEl.attr("data-pid").toInt()))
                    }
                }
            } else {
                for (directorElement in els) {
                    var name = directorElement.select("span a span").text()
                    val idText = directorElement.select("span").attr("data-id")
                    val pidText = directorElement.select("span").attr("data-pid")

                    if (name.isEmpty()) {
                        name = directorElement.text()
                    }
                    if (idText.isNotEmpty() && pidText.isNotEmpty()) {
                        val actor = Actor(idText.toInt(), pidText.toInt())
                        actor.name = name
                        directors.add(actor)
                    }
                }
            }
        }
        film.directors = directors
        film.actors = actors

        val seriesSchedule: ArrayList<Pair<String, ArrayList<Schedule>>> = ArrayList()
        val seasonsElements: Elements = document.select("div.b-post__schedule div.b-post__schedule_block")
        for (block in seasonsElements) {
            var header: String = block.select("div.b-post__schedule_block_title div.title").text()
            val toReplace = "${film.title} - даты выхода серий "

            if (header.contains(toReplace)) {
                header = header.replace("${film.title} - даты выхода серий ", "").dropLast(1)
            }

            val listSchedule: ArrayList<Schedule> = ArrayList()
            val list: Elements = block.select("div.b-post__schedule_list table tbody tr")
            for (el in list) {
                if (el.select("td").hasClass("load-more")) {
                    continue
                }

                val episode: String = el.select("td.td-1").text()
                val name: String = el.select("td.td-2 b").text()
                val watch: Element? = el.selectFirst("td.td-3 i")
                var isWatched = false
                var watchId: Int? = null

                if (watch != null) {
                    isWatched = watch.hasClass("watched")
                    watchId = watch.attr("data-id").toInt()
                }

                val date: String = el.select("td.td-4").text()
                val nextEpisodeIn: String = el.select("td.td-5").text()

                val schedule = Schedule(episode, name, date, isWatched)
                if (nextEpisodeIn.isNotEmpty()) {
                    schedule.nextEpisodeIn = nextEpisodeIn
                }

                schedule.watchId = watchId

                listSchedule.add(schedule)
            }

            seriesSchedule.add(Pair(header, listSchedule))
        }
        film.seriesSchedule = seriesSchedule

        val collectionFilms: ArrayList<Film> = ArrayList()
        val collectionElements: Elements = document.select("div.b-post__partcontent_item")
        for (el in collectionElements) {
            val subFilm: Film

            val a = el.select("div.title a")
            if (a.size > 0) {
                subFilm = Film(a.attr("href"))
                subFilm.title = a.text()
            } else {
                subFilm = Film()
                subFilm.title = el.select("div.title").text()
            }

            subFilm.year = el.select("div.year").text()
            subFilm.ratingKP = el.select("div.rating i").text()
            collectionFilms.add(subFilm)
        }
        film.collection = collectionFilms

        val relatedFilms: ArrayList<Film> = ArrayList()
        val relatedElements = document.select("div.b-content__inline_item")
        for (el in relatedElements) {
            val id = el.attr("data-id")
            val cover: String = el.select("div.b-content__inline_item-cover a img").attr("src")
            val a = el.select("div.b-content__inline_item-link a")
            val link: String = a.attr("href")
            val title: String = a.text()
            val misc: String = el.select("div.misc").text()

            val relatedFilm = Film(id.toInt())
            relatedFilm.filmLink = link
            relatedFilm.posterPath = cover
            relatedFilm.title = title
            relatedFilm.relatedMisc = misc
            relatedFilms.add(relatedFilm)
        }
        film.related = relatedFilms

        val bookmarks: ArrayList<Bookmark> = ArrayList()
        val bookmarkElements = document.select("div.hd-label-row")
        for (el in bookmarkElements) {
            val name = "${el.select("label")[0].ownText()} (${el.select("b").text()})"
            val isChecked = el.select("input").attr("checked") == "checked"
            val catId = el.select("input").attr("value")

            val bookmark = Bookmark(catId, "", name, 0)
            bookmark.isChecked = isChecked
            bookmarks.add(bookmark)
        }
        film.bookmarks = bookmarks

        // get streams
        film.isMovieTranslation = document.select("meta[property=og:type]").first().attr("content").equals("video.movie")
        val filmTranslations: ArrayList<Voice> = ArrayList()
        val els = document.select(".b-translator__item")
        for (el in els) {
            val voice = Voice(el.attr("title"), el.attr("data-translator_id"))
            voice.isAds = el.attr("data-ads")
            voice.isCamrip = el.attr("data-camrip")
            voice.isDirector = el.attr("data-director")
            filmTranslations.add(voice)
        }

        // no film translations
        try {
            if (filmTranslations.size == 0) {
                val stringedDoc = document.toString()

                if (film.isMovieTranslation!!) {
                    val index = stringedDoc.indexOf("initCDNMoviesEvents")
                    val subString = stringedDoc.substring(stringedDoc.indexOf("{\"id\"", index), stringedDoc.indexOf("});", index) + 1)
                    filmTranslations.add(Voice(JSONObject(subString).getString("streams")))
                } else {
                    val startIndex = stringedDoc.indexOf("initCDNSeriesEvents")
                    var endIndex = stringedDoc.indexOf("{\"id\"", startIndex)
                    if (endIndex == -1) {
                        endIndex = stringedDoc.indexOf("{\"url\"", startIndex)
                    }
                    val subString = stringedDoc.substring(startIndex, endIndex)
                    val transId = subString.split(",")[1]
                    filmTranslations.add(Voice(transId, parseSeasons(document)))
                }
            }
        } catch (e: Exception) {
            film.isAwaiting = true
        }

        film.translations = filmTranslations

        film.hasAdditionalData = true

        return film
    }

    var count = 0
    fun getFilmsData(films: ArrayList<Film>, filmsPerPage: Int, iConnection: IConnection, callback: (ArrayList<Film>) -> Unit) {
        val filmsToLoad: ArrayList<Film> = ArrayList()
        for ((index, film) in (films.clone() as ArrayList<Film>).withIndex()) {
            filmsToLoad.add(film)
            films.removeAt(0)

            if (index == filmsPerPage - 1) {
                break
            }
        }

        val loadedFilms = arrayOfNulls<Film?>(filmsPerPage)
        count = 0
        for ((index, film) in filmsToLoad.withIndex()) {
            startFilmLoad(loadedFilms, filmsToLoad, index, film, callback)
        }
    }

    private fun startFilmLoad(loadedFilms: Array<Film?>, filmsToLoad: ArrayList<Film>, index: Int, film: Film, callback: (ArrayList<Film>) -> Unit) {
        GlobalScope.launch {
            try {
                loadedFilms[index] = getMainData(film)

                count++

                if (count >= filmsToLoad.size) {
                    val list: ArrayList<Film> = ArrayList()
                    for (item in loadedFilms) {
                        if (item != null) {
                            list.add(item)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        callback(list)
                    }
                }
            } catch (e: Exception) {
                delay(150)
                startFilmLoad(loadedFilms, filmsToLoad, index, film, callback)
            }
        }
    }

    fun getFilmPosterByLink(filmLink: String): String {
        val filmPage: Document = Jsoup.connect(filmLink).get()
        val posterElement: Element = filmPage.select(FILM_POSTER)[0]
        return posterElement.select("img").attr("src")
    }

    fun postWatch(watchId: Int) {
        val result: Element? = Jsoup.connect(SettingsData.provider + WATCH_ADD)
            .data("id", watchId.toString())
            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .header("Cookie", CookieManager.getInstance().getCookie(SettingsData.provider) + "; allowed_comments=1;")
            .ignoreContentType(true)
            .post()

        if (result != null) {
            val bodyString: String = result.select("body").text()
            val jsonObject = JSONObject(bodyString)

            val isSuccess: Boolean = jsonObject.getBoolean("success")
            if (!isSuccess) {
                throw HttpStatusException("failed to post watch because: ${jsonObject.getString("message")}", 400, SettingsData.provider)
            }
        }
    }

    fun postRating(film: Film, rating: Float) {
        val result: String = Jsoup
            .connect(SettingsData.provider + RATING_ADD + "?news_id=${film.filmId}&go_rate=${rating}&skin=hdrezka")
            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .header("Cookie", CookieManager.getInstance().getCookie(SettingsData.provider))
            .ignoreContentType(true)
            .execute()
            .body()

        val jsonObject = JSONObject(result)
        val isSuccess: Boolean = jsonObject.getBoolean("success")

        if (isSuccess) {
            film.ratingHR = jsonObject.getString("num")
            film.votesHR = jsonObject.getString("votes")
            film.isHRratingActive = !film.isHRratingActive!!
        } else {
            throw HttpStatusException("failed to post comment", 400, SettingsData.provider)
        }
    }

    private fun getStreamsByTranslationId(filmId: Number, translation: Voice): String {
        val data: ArrayMap<String, String> = ArrayMap()
        data["id"] = filmId.toString()
        data["translator_id"] = translation.id
        data["is_camrip"] = translation.isCamrip
        data["is_ads"] = translation.isAds
        data["is_director"] = translation.isDirector
        data["action"] = "get_movie"

        val unixTime = System.currentTimeMillis()
        val result: Document? = Jsoup.connect(SettingsData.provider + GET_STREAM_POST + "/?t=$unixTime")
            .data(data)
            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            //    .header("Cookie", CookieManager.getInstance().getCookie(SettingsData.provider))
            .ignoreContentType(true)
            .post()

        if (result != null) {
            val bodyString: String = result.select("body").text()
            val jsonObject = JSONObject(bodyString)

            if (jsonObject.getBoolean("success")) {
                return jsonObject.getString("url")
            } else {
                throw HttpStatusException("failed to get stream", 400, SettingsData.provider)
            }
        } else {
            throw HttpStatusException("failed to get stream", 400, SettingsData.provider)
        }
    }

    fun parseStreams(translation: Voice, filmId: Number): ArrayList<Stream> {
        val parsedStreams: ArrayList<Stream> = ArrayList()

        if (translation.id != null && translation.streams == null) {
            translation.streams = getStreamsByTranslationId(filmId, translation)
        }

        val split: Array<String> = translation.streams!!.split(",").toTypedArray()
        for (str in split) {
            if (str.contains(" or ")) {
                parsedStreams.add(Stream(str.split(" or ").toTypedArray()[1], str.substring(1, str.indexOf("]"))))
            } else {
                parsedStreams.add(Stream(str.substring(str.indexOf("]") + 1), str.substring(1, str.indexOf("]"))))
            }
        }

        return parsedStreams
    }

    fun getSeason(filmId: Number, translation: Voice): Voice {
        val data: ArrayMap<String, String> = ArrayMap()
        data["id"] = filmId.toString()
        data["translator_id"] = translation.id
        data["action"] = "get_episodes"

        val unixTime = System.currentTimeMillis()
        val result: Document? = Jsoup.connect(SettingsData.provider + GET_STREAM_POST + "/?t=$unixTime")
            .data(data)
            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .ignoreContentType(true)
            .post()

        if (result != null) {
            val bodyString: String = result.select("body").text()
            val jsonObject = JSONObject(bodyString)

            if (jsonObject.getBoolean("success")) {
                translation.seasons = parseSeasons(Jsoup.parse(jsonObject.getString("episodes")))
                return translation
            } else {
                throw HttpStatusException("failed to get seasons", 400, SettingsData.provider)
            }
        } else {
            throw HttpStatusException("failed to get seasons", 400, SettingsData.provider)
        }
    }

    private fun parseSeasons(document: Document): HashMap<String, ArrayList<String>> {
        val seasonList: HashMap<String, ArrayList<String>> = HashMap()
        val seasons = document.select("ul.b-simple_episodes__list")

        for ((i, season) in seasons.withIndex()) {
            val n = season.attr("id").replace("simple-episodes-list-", "")
            val episodesList: ArrayList<String> = ArrayList()
            val episodes = season.select("li.b-simple_episode__item")
            for (episode in episodes) {
                episodesList.add(episode.attr("data-episode_id"))
            }

            seasonList[n] = episodesList
        }

        return seasonList
    }

    fun getStreamsByEpisodeId(translation: Voice, filmId: Int, season: String, episode: String): String {
        val data: ArrayMap<String, String> = ArrayMap()
        data["id"] = filmId.toString()
        data["translator_id"] = translation.id
        data["season"] = season
        data["episode"] = episode
        data["action"] = "get_stream"

        val unixTime = System.currentTimeMillis()
        val result: Document? = Jsoup.connect(SettingsData.provider + GET_STREAM_POST + "/?t=$unixTime")
            .data(data)
            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            //  .header("Cookie", CookieManager.getInstance().getCookie(SettingsData.provider))
            .ignoreContentType(true)
            .post()

        if (result != null) {
            val bodyString: String = result.select("body").text()
            val jsonObject = JSONObject(bodyString)

            if (jsonObject.getBoolean("success")) {
                return jsonObject.getString("url")
            } else {
                throw HttpStatusException("failed to get stream", 400, SettingsData.provider)
            }
        } else {
            throw HttpStatusException("failed to get stream", 400, SettingsData.provider)
        }
    }

    fun saveWatch(filmId: Int, translation: Voice) {
        val data: ArrayMap<String, String> = ArrayMap()
        data["post_id"] = filmId.toString()
        data["translator_id"] = translation.id.toString()
        data["season"] = translation.selectedEpisode?.first ?: "0"
        data["episode"] = translation.selectedEpisode?.second ?: "0"
        data["current_time"] = "1"
        //data["duration"] = "1"

        val unixTime = System.currentTimeMillis()
        val result: Document? = Jsoup.connect(SettingsData.provider + SEND_WATCH + "/?t=$unixTime")
            .data(data)
            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .header("Cookie", CookieManager.getInstance().getCookie(SettingsData.provider))
            .ignoreContentType(true)
            .post()

        if (result != null) {
            val bodyString: String = result.select("body").text()
            val jsonObject = JSONObject(bodyString)

            if (!jsonObject.getBoolean("success")) {
                throw HttpStatusException("failed to save watch", 400, SettingsData.provider)
            }
        } else {
            throw HttpStatusException("failed to save watch", 400, SettingsData.provider)
        }
    }
}
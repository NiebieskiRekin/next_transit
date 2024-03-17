package com.example.nexttransit

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiCaller {
    private val client = HttpClient(Android) {
        install(Logging)
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            }
            )
        }
    }

    suspend fun getDirectionsByName(origin: String, destination: String) : DirectionsResponse{
        val response: DirectionsResponse = client.get {
            url {
                protocol = URLProtocol.HTTPS
                host = "maps.googleapis.com"
                path("/maps/api/directions/json")
                parameters.append("destination",destination)
                parameters.append("origin",origin)
                parameters.append("mode","transit")
                parameters.append("language","pl")
                parameters.append("key", BuildConfig.API_KEY)
            }
        }.body()
        return response
    }

    suspend fun getDirectionsByPlaceId(origin: String, destination: String): DirectionsResponse {
        val response: DirectionsResponse = client.get {
            url {
                protocol = URLProtocol.HTTPS
                host = "maps.googleapis.com"
                path("/maps/api/directions/json")
                parameters.append("destination", "place_id:$destination")
                parameters.append("origin", "place_id:$origin")
                parameters.append("mode", "transit")
                parameters.append("language", "pl")
                parameters.append("key", BuildConfig.API_KEY)
            }
        }.body()

        return response
    }

    fun trimPolyline(directionsResponse: DirectionsResponse) : DirectionsResponse {
        // This is as bad as it gets
        return directionsResponse.copy(
            routes=directionsResponse.routes.map { route ->
                route.copy(
                    overviewPolyline = OverviewPolyline("..."),
                    legs = route.legs.map {leg ->
                        leg.copy(
                            steps=leg.steps.map {bigStep ->
                                bigStep.copy(
                                    polyline = OverviewPolyline("..."),
                                    steps=bigStep.steps?.map { step ->
                                        step.copy(
                                            polyline = OverviewPolyline("...")
                                        )
                                    }
                                )
                            }
                        )
                    }
                )
            }
        )
    }

    fun getSampleDirections() : DirectionsResponse {
        val response = """{
  "geocoded_waypoints" :
  [
    {
      "geocoder_status" : "OK",
      "place_id" : "ChIJLcfSImn7BEcRa3MR7sqwJsw",
      "types" :
      [
        "establishment",
        "point_of_interest",
        "train_station",
        "transit_station"
      ]
    },
    {
      "geocoder_status" : "OK",
      "place_id" : "ChIJC0kwPxJbBEcRaulLN8Dqppc",
      "types" :
      [
        "premise"
      ]
    }
  ],
  "routes" :
  [
    {
      "bounds" :
      {
        "northeast" :
        {
          "lat" : 52.4019862,
          "lng" : 17.275247
        },
        "southwest" :
        {
          "lat" : 52.2178422,
          "lng" : 16.9307298
        }
      },
      "copyrights" : "Map data ©2023 Google",
      "legs" :
      [
        {
          "arrival_time" :
          {
            "text" : "6:42 PM",
            "time_zone" : "Europe/Warsaw",
            "value" : 1696783329
          },
          "departure_time" :
          {
            "text" : "5:42 PM",
            "time_zone" : "Europe/Warsaw",
            "value" : 1696779720
          },
          "distance" :
          {
            "text" : "33.6 km",
            "value" : 33567
          },
          "duration" :
          {
            "text" : "1 hour 0 mins",
            "value" : 3609
          },
          "end_address" : "Politechnika Poznańska, Kampus Piotrowo, Piotrowo 3, 61-001 Poznań, Poland",
          "end_location" :
          {
            "lat" : 52.4019862,
            "lng" : 16.9519485
          },
          "start_address" : "Środa Wielkopolska, 63-000 Środa Wielkopolska, Poland",
          "start_location" :
          {
            "lat" : 52.21811779999999,
            "lng" : 17.275247
          },
          "steps" :
          [
            {
              "distance" :
              {
                "text" : "28.7 km",
                "value" : 28731
              },
              "duration" :
              {
                "text" : "23 mins",
                "value" : 1380
              },
              "end_location" :
              {
                "lat" : 52.36644279999999,
                "lng" : 16.9346573
              },
              "html_instructions" : "Train towards Poznań Główny",
              "polyline" :
              {
                "points" : "gzu}HiamhBv@`AoCbIaHvQWl@CFiDlJi@|AUn@yA`EiFhOkEhMs@pBqElM{AbEITwDvKy@rCGVm@rBY~@e@lByBtJcBfIe@vBoBpJmBvIoD|P{@vDiErS{G`\\q@`CSv@Y|@Sl@Wr@m@`B_BxDy@hBuElKk@nA}DlJuIrSwDxImAvCoClGyEvKgB~ECHiAlD}A|FOl@CJsAjF{@jD_Ir[kBtHaCzJKZaDdMk@bC}CxMsAvFKb@uDzNgIx[e@jBaAxDqArEoAzEsApFg@pBoAbFqAhFaEtP_EtPmLhe@qBfIiAlEq@nCmA|EGX}Ljg@}Iv^yI`^_Kpa@iHrYGZiJh_@oFvUSz@Kb@cCtJaAxD}EpR_DfMsDnOCHK\\{AnGwBrIyBbJeDxMm@dC{DdP{@jDcAfEc@fBK^eAfEk@|B}EvS{@jD{I~]uEbR{Rfx@CHmY|kAgDhNg@pBuHp[ENUz@sExQa@`Bm@hCeCrJETG`@GZMj@Ml@iCvKiDpNuAtFKb@_BtGyAzFcAfEcC~JoAnFk@bCcCvKCHsAzEcAhDkBrFaA`CaBtDuElJ]r@qDbHcB`DcJlQoBxDsB|D}BpEYh@_@v@{AzCQZsB`EYh@Sb@sAhC{BnEmBvD{AvCoBxDmBtDUb@A@aKdSg@`A}BpE_@r@cGrLuAlCWf@_B~CEH{@`B_AlB_AjB}DrHgAtB]r@q@rAqDjHcBbDe@`AkAzBABaAlBuBfE}BpEg@`AuI|PuAjCmCnFe@~@g@~@o@pAi@dAe@`Ao@jAy@bBk@dA_ExH_CxEkG`M_B|CaBbDs@pAc@z@IRaB`DcAlBqDlH{ChGmA`C{BnE_@r@cJhQgC~EaBdDUf@qBzDaCtE{@dBoAdCoBtDKRm@lAsCtFGJiBjDm@lAcBjD}BnEq@rAq@rAoCpF{BnEe@z@{IdQYh@MXoBvDWh@_CpEYj@iCfF}BrEsFtKy@`By@~AwApC]p@yBjEw@xAYn@eC~Es@tAKREHoMhWEHo@lAcHzNQ^u@dBoAhDCH{@cA"
              },
              "start_location" :
              {
                "lat" : 52.21811779999999,
                "lng" : 17.275247
              },
              "transit_details" :
              {
                "arrival_stop" :
                {
                  "location" :
                  {
                    "lat" : 52.36644279999999,
                    "lng" : 16.9346573
                  },
                  "name" : "Poznań Starołęka"
                },
                "arrival_time" :
                {
                  "text" : "6:05 PM",
                  "time_zone" : "Europe/Warsaw",
                  "value" : 1696781100
                },
                "departure_stop" :
                {
                  "location" :
                  {
                    "lat" : 52.21811779999999,
                    "lng" : 17.275247
                  },
                  "name" : "Środa Wielkopolska"
                },
                "departure_time" :
                {
                  "text" : "5:42 PM",
                  "time_zone" : "Europe/Warsaw",
                  "value" : 1696779720
                },
                "headsign" : "Poznań Główny",
                "line" :
                {
                  "agencies" :
                  [
                    {
                      "name" : "Koleje Wielkopolskie",
                      "phone" : "011 48 61 279 27 78",
                      "url" : "http://kolejewlkp.pl/"
                    }
                  ],
                  "short_name" : "KW",
                  "vehicle" :
                  {
                    "icon" : "//maps.gstatic.com/mapfiles/transit/iw2/6/rail2.png",
                    "name" : "Train",
                    "type" : "HEAVY_RAIL"
                  }
                },
                "num_stops" : 5,
                "trip_short_name" : "77233 EZT"
              },
              "travel_mode" : "TRANSIT"
            },
            {
              "distance" :
              {
                "text" : "0.5 km",
                "value" : 493
              },
              "duration" :
              {
                "text" : "7 mins",
                "value" : 414
              },
              "end_location" :
              {
                "lat" : 52.3688458,
                "lng" : 16.9323756
              },
              "html_instructions" : "Walk to Starołęka PKM",
              "polyline" :
              {
                "points" : "wwr~HynjfBUp@_AxCe@nBg@vBcAvEG^KIMGGb@GX]]wEgF?SAM@M?KAG"
              },
              "start_location" :
              {
                "lat" : 52.3661977,
                "lng" : 16.9343673
              },
              "steps" :
              [
                {
                  "distance" :
                  {
                    "text" : "0.3 km",
                    "value" : 268
                  },
                  "duration" :
                  {
                    "text" : "4 mins",
                    "value" : 215
                  },
                  "end_location" :
                  {
                    "lat" : 52.3673988,
                    "lng" : 16.9309504
                  },
                  "html_instructions" : "Head \u003cb\u003enorthwest\u003c/b\u003e\u003cdiv style=\"font-size:0.9em\"\u003eTake the stairs\u003c/div\u003e",
                  "polyline" :
                  {
                    "points" : "wwr~HynjfBUp@_AxCe@nBg@vBcAvEG^"
                  },
                  "start_location" :
                  {
                    "lat" : 52.3661977,
                    "lng" : 16.9343673
                  },
                  "travel_mode" : "WALKING"
                },
                {
                  "distance" :
                  {
                    "text" : "15 m",
                    "value" : 15
                  },
                  "duration" :
                  {
                    "text" : "1 min",
                    "value" : 12
                  },
                  "end_location" :
                  {
                    "lat" : 52.3675258,
                    "lng" : 16.9310418
                  },
                  "html_instructions" : "Take the pedestrian tunnel",
                  "polyline" :
                  {
                    "points" : "g_s~HmyifBKIMG"
                  },
                  "start_location" :
                  {
                    "lat" : 52.3673988,
                    "lng" : 16.9309504
                  },
                  "travel_mode" : "WALKING"
                },
                {
                  "distance" :
                  {
                    "text" : "23 m",
                    "value" : 23
                  },
                  "duration" :
                  {
                    "text" : "1 min",
                    "value" : 26
                  },
                  "end_location" :
                  {
                    "lat" : 52.3676052,
                    "lng" : 16.9307298
                  },
                  "html_instructions" : "Turn \u003cb\u003eleft\u003c/b\u003e toward \u003cb\u003eStarołęcka\u003c/b\u003e\u003cdiv style=\"font-size:0.9em\"\u003eTake the stairs\u003c/div\u003e",
                  "maneuver" : "turn-left",
                  "polyline" :
                  {
                    "points" : "a`s~H_zifBGb@GX"
                  },
                  "start_location" :
                  {
                    "lat" : 52.3675258,
                    "lng" : 16.9310418
                  },
                  "travel_mode" : "WALKING"
                },
                {
                  "distance" :
                  {
                    "text" : "0.2 km",
                    "value" : 164
                  },
                  "duration" :
                  {
                    "text" : "2 mins",
                    "value" : 134
                  },
                  "end_location" :
                  {
                    "lat" : 52.3688422,
                    "lng" : 16.9320418
                  },
                  "html_instructions" : "Turn \u003cb\u003eright\u003c/b\u003e onto \u003cb\u003eStarołęcka\u003c/b\u003e",
                  "maneuver" : "turn-right",
                  "polyline" :
                  {
                    "points" : "q`s~HaxifB]]wEgF"
                  },
                  "start_location" :
                  {
                    "lat" : 52.3676052,
                    "lng" : 16.9307298
                  },
                  "travel_mode" : "WALKING"
                },
                {
                  "distance" :
                  {
                    "text" : "23 m",
                    "value" : 23
                  },
                  "duration" :
                  {
                    "text" : "1 min",
                    "value" : 27
                  },
                  "end_location" :
                  {
                    "lat" : 52.3688458,
                    "lng" : 16.9323756
                  },
                  "html_instructions" : "Turn \u003cb\u003eright\u003c/b\u003e onto \u003cb\u003eForteczna\u003c/b\u003e\u003cdiv style=\"font-size:0.9em\"\u003eDestination will be on the right\u003c/div\u003e",
                  "maneuver" : "turn-right",
                  "polyline" :
                  {
                    "points" : "ghs~Hg`jfB?SAM@M?KAG"
                  },
                  "start_location" :
                  {
                    "lat" : 52.3688422,
                    "lng" : 16.9320418
                  },
                  "travel_mode" : "WALKING"
                }
              ],
              "travel_mode" : "WALKING"
            },
            {
              "distance" :
              {
                "text" : "4.0 km",
                "value" : 4045
              },
              "duration" :
              {
                "text" : "14 mins",
                "value" : 840
              },
              "end_location" :
              {
                "lat" : 52.4007011,
                "lng" : 16.9487645
              },
              "html_instructions" : "Tram towards Aleje Marcinkowskiego",
              "polyline" :
              {
                "points" : "mfs~HobjfBB^[JI@G?GAA?ECEECAAAQQIIcAiAiAkA{@}@u@u@cAgAEE[_@cBiBSUoAqAuAwAcBoBmCwCgAmAcAgA_@c@c@e@q@s@q@u@w@}@w@}@wAeBIG_@a@SUGGu@{@Y[_@a@_AcAUU]a@_AcA_A{@YWYSi@][Oc@So@S_AWICq@UWGICG@O?QGA?EAWIMCKE]K_@M[GWCm@EmAMaAIoAMa@Cs@Ie@Ic@Kq@Qi@Qe@UOG_Aa@oB{@o@Y}@_@QKg@SGCw@]g@QUKoAe@sAe@{Bw@aC}@MEkC}@iAa@{@[i@Sa@OEAu@Yk@SAA_@Mm@Wo@YQIQKA?q@]oAq@wAs@o@]i@Wk@[o@]m@[kAk@mAq@s@_@YO_@SECMGQK]QOGMIy@_@SKc@Ug@UiAi@eAi@uAo@kB_A_CgAYOWMa@QUKWMIXAFCBCFCNET]vASr@Oj@Sr@WdACFW`Ag@tBc@fBU|@KI"
              },
              "start_location" :
              {
                "lat" : 52.36855019999999,
                "lng" : 16.9323966
              },
              "transit_details" :
              {
                "arrival_stop" :
                {
                  "location" :
                  {
                    "lat" : 52.4007011,
                    "lng" : 16.9487645
                  },
                  "name" : "Politechnika"
                },
                "arrival_time" :
                {
                  "text" : "6:38 PM",
                  "time_zone" : "Europe/Warsaw",
                  "value" : 1696783080
                },
                "departure_stop" :
                {
                  "location" :
                  {
                    "lat" : 52.36855019999999,
                    "lng" : 16.9323966
                  },
                  "name" : "Starołęka PKM"
                },
                "departure_time" :
                {
                  "text" : "6:24 PM",
                  "time_zone" : "Europe/Warsaw",
                  "value" : 1696782240
                },
                "headsign" : "Aleje Marcinkowskiego",
                "line" :
                {
                  "agencies" :
                  [
                    {
                      "name" : "Miejskie Przedsiębiorstwo Komunikacyjne Sp. z o.o. w Poznaniu",
                      "phone" : "011 48 61 646 33 44",
                      "url" : "http://www.mpk.poznan.pl/"
                    }
                  ],
                  "color" : "#ad841f",
                  "name" : "Aleje Marcinkowskiego - Starołęka Pkm|starołęka PKM - Aleje Marcinkowskiego",
                  "short_name" : "13",
                  "text_color" : "#ffffff",
                  "vehicle" :
                  {
                    "icon" : "//maps.gstatic.com/mapfiles/transit/iw2/6/tram2.png",
                    "name" : "Tram",
                    "type" : "TRAM"
                  }
                },
                "num_stops" : 11
              },
              "travel_mode" : "TRANSIT"
            },
            {
              "distance" :
              {
                "text" : "0.3 km",
                "value" : 298
              },
              "duration" :
              {
                "text" : "4 mins",
                "value" : 249
              },
              "end_location" :
              {
                "lat" : 52.4019862,
                "lng" : 16.9519485
              },
              "html_instructions" : "Walk to Politechnika Poznańska, Kampus Piotrowo, Piotrowo 3, 61-001 Poznań, Poland",
              "polyline" :
              {
                "points" : "}ny~HyjmfB@AOU[Yw@u@kAgADO\\oA@I?E@C?GAKAMAEAEAE?E@E?CBG?I@I@I@MI?IECGGQk@s@UYW_@OOAA"
              },
              "start_location" :
              {
                "lat" : 52.4006258,
                "lng" : 16.9490861
              },
              "steps" :
              [
                {
                  "distance" :
                  {
                    "text" : "0.1 km",
                    "value" : 115
                  },
                  "duration" :
                  {
                    "text" : "2 mins",
                    "value" : 94
                  },
                  "end_location" :
                  {
                    "lat" : 52.4014993,
                    "lng" : 16.9499722
                  },
                  "html_instructions" : "Head \u003cb\u003enortheast\u003c/b\u003e",
                  "polyline" :
                  {
                    "points" : "}ny~HyjmfB@AOU[Yw@u@kAgA"
                  },
                  "start_location" :
                  {
                    "lat" : 52.4006258,
                    "lng" : 16.9490861
                  },
                  "travel_mode" : "WALKING"
                },
                {
                  "distance" :
                  {
                    "text" : "87 m",
                    "value" : 87
                  },
                  "duration" :
                  {
                    "text" : "1 min",
                    "value" : 74
                  },
                  "end_location" :
                  {
                    "lat" : 52.4012917,
                    "lng" : 16.9511504
                  },
                  "html_instructions" : "Turn \u003cb\u003eright\u003c/b\u003e",
                  "maneuver" : "turn-right",
                  "polyline" :
                  {
                    "points" : "kty~HipmfBDO\\oA@I?E@C?GAKAMAEAEAE?E@E?CBG?I@I@I@M"
                  },
                  "start_location" :
                  {
                    "lat" : 52.4014993,
                    "lng" : 16.9499722
                  },
                  "travel_mode" : "WALKING"
                },
                {
                  "distance" :
                  {
                    "text" : "0.1 km",
                    "value" : 96
                  },
                  "duration" :
                  {
                    "text" : "1 min",
                    "value" : 81
                  },
                  "end_location" :
                  {
                    "lat" : 52.4019862,
                    "lng" : 16.9519485
                  },
                  "html_instructions" : "Turn \u003cb\u003eleft\u003c/b\u003e\u003cdiv style=\"font-size:0.9em\"\u003eDestination will be on the left\u003c/div\u003e",
                  "maneuver" : "turn-left",
                  "polyline" :
                  {
                    "points" : "asy~HuwmfBI?IECGGQk@s@UYW_@OOAA"
                  },
                  "start_location" :
                  {
                    "lat" : 52.4012917,
                    "lng" : 16.9511504
                  },
                  "travel_mode" : "WALKING"
                }
              ],
              "travel_mode" : "WALKING"
            }
          ],
          "traffic_speed_entry" : [],
          "via_waypoint" : []
        }
      ],
      "overview_polyline" :
      {
        "points" : "gzu}HiamhBv@`AoCbIyHdSmFbOcIjU_GzPoN~`@iC~I_DbNiC~LoBpJmBvIkFtVeNtp@eAxDm@jBeAtCyCbHaG|MyWrn@iJdTkBhFiAlD}A|FSx@{P`r@mCvKmEhQqFpUaE~OmJd_@aAxDqArEcDlMwBtIsG~WmR~v@{Hb[eMdh@wTx}@{_@j}AcGrWoCxK_HjXsIv]cFjSiOhn@oDzNqBdIyGbYqPbq@_Spx@si@j{BqGfWsD|NMv@UfAwCdMkJ``@}CbMsEnRoDzOwAdFcAhDkBrFcDvHsF`LuGdMeUvc@gDxGkTrb@q]|q@mQn]aPn[}\\bq@gDxGuInPmQ|]cEbI_NfXgSl`@wBlEsFpKgGtLsHzNaI|OwYrk@aXzh@gObZkOjZuHzOu@dBoAhDCH{@cAn@x@Up@_AxCe@nBkBnIG^KIMGGb@GX]]wEgF?S?[ASz@CB^e@LWE][sE}E{CcD}GiH}J}KgDsDqEiFaGsG}AeByAsAcAq@_Ac@kDeAa@KW@q@Sw@U{@UeAIaGi@yASuA]_Bo@wKyEaFiBwKyD{HsCoCiAuC{AuM}GaEuBwHuDkLsFWMIXEJk@dCoAxEgBfHU|@KINcAk@o@cC}Bd@oBAe@EWD[BS@MI?MMGQk@s@m@y@QQ"
      },
      "summary" : "",
      "warnings" :
      [
        "Walking directions are in beta. Use caution – This route may be missing sidewalks or pedestrian paths."
      ],
      "waypoint_order" : []
    }
  ],
  "status" : "OK"
}"""
        return Json.decodeFromString(response)
    }
}
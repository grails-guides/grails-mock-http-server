//tag::classdeclaration[]
package org.openweathermap

import com.stehno.ersatz.ErsatzServer
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class OpenweathermapServiceSpec extends Specification implements ServiceUnitTest<OpenweathermapService> {
//end::classdeclaration[]

    //tag::401[]
    def "For an unauthorized key, null is return"() {
        given:
        ErsatzServer ersatz = new ErsatzServer()
        String city = 'London'
        String countryCode = 'uk'
        String appid = 'XXXXX'
        ersatz.expectations {
            get('/data/2.5/weather') { // <1>
                query('q', "${city},${countryCode}")
                query('appid', appid)
                called(1) // <2>
                responder {
                    code(401) // <3>
                }
            }
        }
        service.openWeatherUrl = ersatz.httpUrl // <4>
        service.appid = appid

        when:
        CurrentWeather currentWeather = service.currentWeather(city, countryCode)

        then:
        !currentWeather

        and:
        ersatz.verify() // <5>

        cleanup:
        ersatz.stop() // <6>
    }
    //end::401[]

    //tag::200[]
    def "A CurrentWeather object is built from JSON Payload"() {
        given:
        ErsatzServer ersatz = new ErsatzServer()
        String city = 'London'
        String countryCode = 'uk'
        String appid = 'XXXXX'
        String expected = '''{"coord":{"lon":-0.13,"lat":51.51},"weather":[{"id":803,"main":"Clouds","description":"broken clouds","icon":"04d"}],"base":"stations","main":{"temp":20.81,"pressure":1017,"humidity":53,"temp_min":19,"temp_max":22},"visibility":10000,"wind":{"speed":3.6,"deg":180,"gust":9.8},"clouds":{"all":75},"dt":1502707800,"sys":{"type":1,"id":5091,"message":0.0029,"country":"GB","sunrise":1502685920,"sunset":1502738622},"id":2643743,"name":"London","cod":200}'''
        ersatz.expectations {
            get('/data/2.5/weather') {
                query('q',"${city},${countryCode}")
                query('appid', appid)
                called(1)
                responder {
                    code(200)
                    content expected,'application/json'
                }
            }
        }
        service.openWeatherUrl = ersatz.httpUrl
        service.appid = appid

        when:
        CurrentWeather currentWeather = service.currentWeather(city, countryCode)

        then:
        currentWeather
        currentWeather.weatherList[0].main == 'Clouds'
        currentWeather.cityName == 'London'
        currentWeather.code == 200
        currentWeather.cityId == 2643743
        currentWeather.main.temperature == 20.81
        currentWeather.main.pressure == 1017
        currentWeather.main.humidity == 53
        currentWeather.main.tempMin == 19
        currentWeather.main.tempMax == 22
        currentWeather.weatherList[0].id == 803
        currentWeather.weatherList[0].main == 'Clouds'
        currentWeather.weatherList[0].description == 'broken clouds'
        currentWeather.weatherList[0].icon == '04d'
        currentWeather.visibility == 10000
        currentWeather.wind.speed == 3.6
        currentWeather.wind.deg == 180
        currentWeather.clouds.cloudiness == 75
        currentWeather.base == 'stations'
        currentWeather.dt == 1502707800
        currentWeather.coordinate

        and:
        ersatz.verify()

        cleanup:
        ersatz.stop()
    }
    //end::200[]
}

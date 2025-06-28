rootProject.name = "hexa-example"

// Car 도메인 모듈
include("car:application:domain")
include("car:application:exception")
include("car:application:port-in")
include("car:application:port-out")
include("car:application:service")
include("car:adapter-jpa")
include("car:adapter-rest")

// ParkingLot 도메인 모듈
include("parking-lot:application:domain")
include("parking-lot:application:port-in")
include("parking-lot:application:service")
include("parking-lot:adapter-rest")
include("parking-lot:adapter-cron")

// 애플리케이션 모듈
include("application-api")
include("application-cron")
include("bootstrap")

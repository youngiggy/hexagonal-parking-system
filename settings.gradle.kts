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

// ParkingLot JPA 어댑터
include("parking-lot-adapter-jpa")

rootProject.name = "hexagonal-parking-system"

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

// ParkingLot 어댑터
include("parking-lot-adapter-jpa")
include("parking-lot-adapter-rest")

// 통합 테스트
include("parking-lot-adapter-integration-test")

// 통합 애플리케이션
include("integrated-parking-app")
include("integration-test")

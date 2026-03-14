// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "Tradeguruelectrical",
    platforms: [.iOS(.v18)],
    products: [
        .library(name: "Tradeguruelectrical", targets: ["Tradeguruelectrical"]),
    ],
    dependencies: [],
    targets: [
        .target(name: "Tradeguruelectrical", dependencies: [], path: "Tradeguruelectrical"),
    ]
)

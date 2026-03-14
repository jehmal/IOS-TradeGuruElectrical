import SwiftUI

extension Color {
    static let tradeGreen = Color(red: 32/255, green: 171/255, blue: 110/255)

    static let tradeSurface = Color(light: Color(hex: 0xF7F2F9), dark: Color(hex: 0x2F2D32))
    static let tradeInput = Color(light: Color(hex: 0xEEE9F0), dark: Color(hex: 0x3D3A40))
    static let tradeLight = Color(light: Color(hex: 0xFFFCFF), dark: Color(hex: 0x2F2D32))
    static let tradeBorder = Color(light: Color(hex: 0xB8B3BA), dark: Color(hex: 0x6B7280))
    static let tradeText = Color(light: Color(hex: 0x242026), dark: .white)
    static let tradeTextSecondary = Color(light: Color(hex: 0x6B7280), dark: Color(hex: 0x9CA3AF))
    static let tradeBg = Color(light: .white, dark: Color(hex: 0x1A1A1C))

    static let modeFaultFinder = Color(light: Color(hex: 0xF59E0B), dark: Color(hex: 0xFBBF24))
    static let modeLearn = Color(light: Color(hex: 0x3B82F6), dark: Color(hex: 0x60A5FA))
    static let modeResearch = Color(light: Color(hex: 0x8B5CF6), dark: Color(hex: 0xA78BFA))
}

extension Color {
    init(hex: UInt, opacity: Double = 1.0) {
        self.init(
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255,
            opacity: opacity
        )
    }

    init(light: Color, dark: Color) {
        self.init(uiColor: UIColor { traits in
            traits.userInterfaceStyle == .dark
                ? UIColor(dark)
                : UIColor(light)
        })
    }
}

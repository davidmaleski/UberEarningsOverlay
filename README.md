# UberEarningsOverlay
**UberEarningsOverlay** é um aplicativo Android que exibe uma sobreposição (overlay) na tela para calcular os ganhos estimados de motoristas da Uber com base na distância de uma corrida. O app utiliza um `AccessibilityService` para capturar a distância exibida no app da Uber Driver (ex.: "5.2 km") e multiplica pela taxa por km configurada pelo usuário (ex.: R$ 1,80/km), mostrando o resultado em tempo real (ex.: "R$ 9,36").

## 📋 Funcionalidades

- **Sobreposição em Tempo Real**: Exibe uma sobreposição arrastável na tela com os ganhos estimados (ex.: "R$ 9,36").
- **Captura de Distância**: Usa um `AccessibilityService` para capturar a distância de corridas no app da Uber Driver.
- **Taxa Configurável**: Permite ao usuário definir a taxa por km (ex.: R$ 1,80/km) em uma tela de configurações.
- **Ativar/Desativar**: Inclui uma tela inicial com botões para ativar/desativar a sobreposição e acessar as configurações.

## 📸 Capturas de Tela

*(Adicione capturas de tela do app aqui, se possível. Você pode tirar prints do app no celular ou emulador e colocá-los na pasta `screenshots/` do repositório.)*

## 🚀 Como Usar

### Pré-requisitos

- **Dispositivo Android**: Android 6.0 (API 23) ou superior.
- **App da Uber Driver**: O app da Uber Driver deve estar instalado para que o `AccessibilityService` possa capturar a distância.
- **Android Studio**: Para compilar e modificar o projeto (recomendado: Android Studio 2023.1.1 ou superior).

### Instalação

1. **Clone o Repositório**:
   ```bash
   git clone https://github.com/seu-usuario/UberEarningsOverlay.git
   cd UberEarningsOverlay

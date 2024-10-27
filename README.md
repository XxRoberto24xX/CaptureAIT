# CaptureAIT

**CaptureAIT** is a competitive mobile game where players compete to locate specific physical objects and capture photos of them within a given time frame. Using AI, the system detects the presence of these objects in the photos and awards points based on the speed of discovery relative to other players. The player with the most points at the end of the game wins!

## Table of Contents

- [About the Project](#about-the-project)
- [Technologies](#technologies)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

---

## About the Project

CaptureAIT aims to combine real-world exploration with AI-powered object detection for a unique, interactive gaming experience. The game requires participants to search for four specific objects and take photos of them. The system utilizes the COCO neural network model to identify objects in the images and score players based on their speed in finding each item.

This repository contains the core code for the Android client and Python server; however, the Firebase database is not included, as players are encouraged to set up their own Firebase integration.

## Technologies

This project uses:

- **Android Client**:
  - Developed in **Java** using **Android Studio**.
  - HTTP requests powered by **OkHttp**.
  - Real-time communication via **Socket.IO**.
- **Python Server**:
  - Built with **Flask** to handle incoming HTTP and WebSocket connections.
  - Uses a COCO neural network model to detect objects in player photos.
- **Database**:
  - **Firebase** (not included) is required for data storage and retrieval. Anyone interested in testing the project can easily set up Firebase using Android Studio’s built-in integration wizard.

## Getting Started

To set up and run the project locally:

1. **Clone the Repository**:  
   ```bash
   git clone https://github.com/yourusername/CaptureAIT.git
   cd CaptureAIT
   ```
2. **Android Client**:  
   - Open Android Studio, load the project, and set up Firebase by following the integration steps provided by Android Studio.
   - Add your Firebase project configuration file (`google-services.json`) in the appropriate directory to enable Firebase functionality.

3. **Python Server**:  
   - Ensure Python 3.x is installed.
   - Install required dependencies:
     ```bash
     pip install -r requirements.txt
     ```
   - Start the Flask server:
     ```bash
     python server.py
     ```
## Usage

- **Start a New Game**: Players can initiate a new game session through the Android app.
- **Object Detection and Scoring**: As players upload photos, the server checks for the specified objects using the COCO model and calculates points based on the speed of identification.
- **Winning the Game**: At the end of the game session, the player with the highest score is declared the winner.

## Contributing

Contributions are welcome! By submitting a contribution, you agree that your contributions will be made under the same **Creative Commons Attribution-NonCommercial (CC BY-NC)** license as the rest of the project.

Please fork the repository and create a pull request with your improvements.

## License

This project is licensed under the **Creative Commons Attribution-NonCommercial (CC BY-NC)** license. You are free to:

- **Share**: Copy and redistribute the material in any medium or format.
- **Adapt**: Remix, transform, and build upon the material.

**Under the following terms**:

- **Attribution**: You must give appropriate credit, provide a link to the license, and indicate if changes were made.
- **NonCommercial**: You may not use the material for commercial purposes without prior written consent.



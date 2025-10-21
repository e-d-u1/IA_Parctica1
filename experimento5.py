import subprocess
import os
import time
import numpy as np
import matplotlib.pyplot as plt

# --- Parámetros del experimento ---
numExp = 5
configuraciones = [
    {"numCentros": 10, "camionesPorCentro": 1},
    {"numCentros": 5, "camionesPorCentro": 2}
]
numGasolineras = 100
reps = 13  # repeticiones por configuración
result_folder = "resultados"

os.makedirs(result_folder, exist_ok=True)

# --- Almacenar resultados ---
resultados_beneficio = {}
resultados_distancia = {}
resultados_peticiones = {}

for cfg in configuraciones:
    label = f"{cfg['numCentros']}c_{cfg['camionesPorCentro']}cam"
    beneficios = []
    distancias = []
    peticiones = []

    for r in range(reps):
        print(f"Ejecutando HC con {label}, repetición {r+1}")
        cmd = [
            "java", "-cp", "lib/*;src", "Main",
            str(numExp), "HC",
            str(cfg["numCentros"]),
            str(cfg["camionesPorCentro"])
        ]
        subprocess.run(cmd)
        time.sleep(0.3)  # esperar para nombres de archivo únicos

        # Leer último archivo generado
        files = sorted([f for f in os.listdir(result_folder) if f.startswith(f"exp{numExp}_")])
        if not files:
            continue
        latest_file = os.path.join(result_folder, files[-1])

        try:
            with open(latest_file, "r") as f:
                lines = [l.strip() for l in f.readlines() if not l.startswith("#") and l.strip() != ""]
                beneficios.append(float(lines[4]))
                distancias.append(float(lines[5]))
                peticiones.append(int(lines[16]))
        except Exception as e:
            print(f"⚠️ Error leyendo {latest_file}: {e}")

    resultados_beneficio[label] = beneficios
    resultados_distancia[label] = distancias
    resultados_peticiones[label] = peticiones

# --- Graficar boxplots comparativos ---
fig, axes = plt.subplots(1, 3, figsize=(18,6))

# Beneficio
data = [resultados_beneficio[l] for l in resultados_beneficio]
box = axes[0].boxplot(data, patch_artist=True)
colors = ["skyblue", "lightgreen"]
for patch, color in zip(box['boxes'], colors):
    patch.set_facecolor(color)
axes[0].set_xticklabels(resultados_beneficio.keys())
axes[0].set_ylabel("Beneficio")
axes[0].set_title("Beneficio HC")

# Distancia
data = [resultados_distancia[l] for l in resultados_distancia]
box = axes[1].boxplot(data, patch_artist=True)
for patch, color in zip(box['boxes'], colors):
    patch.set_facecolor(color)
axes[1].set_xticklabels(resultados_distancia.keys())
axes[1].set_ylabel("Distancia total recorrida")
axes[1].set_title("Distancia HC")

# Peticiones asignadas
data = [resultados_peticiones[l] for l in resultados_peticiones]
box = axes[2].boxplot(data, patch_artist=True)
for patch, color in zip(box['boxes'], colors):
    patch.set_facecolor(color)
axes[2].set_xticklabels(resultados_peticiones.keys())
axes[2].set_ylabel("Peticiones servidas")
axes[2].set_title("Peticiones HC")

plt.suptitle("Experimento 5: Comparación 10c1cam vs 5c2cam")
plt.show()

import subprocess
import os
import time
import numpy as np
import matplotlib.pyplot as plt

# --- Configuración del experimento ---
numExp = 2
estrategias_list = [1, 2]  # 1 = simple, 2 = compleja
estrategias_nombres = {1: "Simple", 2: "Compleja"}
reps = 10
result_folder = "resultados"

os.makedirs(result_folder, exist_ok=True)

# --- Ejecutar los experimentos y leer beneficios ---
datos_estrategias = {1: [], 2: []}

for estrategia in estrategias_list:
    for r in range(reps):
        seed = int(time.time() * 1000) % 100000 + r
        cmd = ["java", "-cp", "lib/*;src", "Main",
               str(numExp), "HC", str(estrategia), str(seed)]
        print(f"Ejecutando estrategia {estrategia} ({estrategias_nombres[estrategia]}), repetición {r+1}")
        subprocess.run(cmd)
        time.sleep(0.2)

        # Leer el último archivo generado
        archivos = sorted([f for f in os.listdir(result_folder) if f.startswith(f"exp{numExp}_")])
        if not archivos:
            print("⚠️ No se generó ningún archivo.")
            continue

        latest_file = os.path.join(result_folder, archivos[-1])
        try:
            with open(latest_file, "r") as f:
                lines = [l.strip() for l in f.readlines() if not l.startswith("#") and l.strip() != ""]
                beneficio = float(lines[4])  # línea 5 de datos -> beneficio final
                datos_estrategias[estrategia].append(beneficio)
        except Exception as e:
            print(f"⚠️ Error leyendo {latest_file}: {e}")

# --- Boxplot ---
data_to_plot = [datos_estrategias[1], datos_estrategias[2]]
fig, ax = plt.subplots(figsize=(8,6))

colors = ['skyblue', 'salmon']
bplot = ax.boxplot(data_to_plot, patch_artist=True, labels=[estrategias_nombres[1], estrategias_nombres[2]])

for patch, color in zip(bplot['boxes'], colors):
    patch.set_facecolor(color)

ax.set_ylabel("Beneficio")
ax.set_title("Comparación de estrategias de generación de solución inicial")
ax.grid(True)
plt.show()

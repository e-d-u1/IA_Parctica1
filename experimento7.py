import subprocess
import os
import time
import numpy as np
import matplotlib.pyplot as plt

# --- Parámetros ---
numExp = 7
velocidad = 80  # km/h
reps = 5
horas = np.arange(0.5, 24.5, 0.5)  # de 0.5h a 24h
result_folder = "resultados"
os.makedirs(result_folder, exist_ok=True)

# --- Almacenar resultados ---
resultados = {}

for h in horas:
    km_max = h * velocidad  # convertir horas a km
    beneficios = []

    for r in range(reps):
        print(f"Ejecutando HC con {h}h ({km_max} km), repetición {r+1}")
        cmd = ["java", "-cp", "lib/*;src", "Main", str(numExp), "HC", str(int(km_max))]
        subprocess.run(cmd)
        time.sleep(0.3)

        files = sorted([f for f in os.listdir(result_folder) if f.startswith(f"exp{numExp}_")])
        if not files:
            continue
        latest_file = os.path.join(result_folder, files[-1])

        try:
            with open(latest_file, "r") as f:
                lines = [l.strip() for l in f.readlines() if not l.startswith("#") and l.strip() != ""]
                beneficio = float(lines[4])
                beneficios.append(beneficio)
        except Exception as e:
            print(f"⚠️ Error leyendo {latest_file}: {e}")

    if beneficios:
        resultados[h] = beneficios

# --- Normalizar beneficios ---
all_vals = [b for v in resultados.values() for b in v]
max_val = max(all_vals)
resultados_norm = {h: [b / max_val for b in v] for h, v in resultados.items()}

# --- Graficar boxplots ---
data = [resultados_norm[h] for h in horas]
plt.figure(figsize=(12,6))
box = plt.boxplot(data, patch_artist=True)

# Colores para las cajas
colors = plt.cm.viridis(np.linspace(0,1,len(horas)))
for patch, color in zip(box['boxes'], colors):
    patch.set_facecolor(color)

plt.xticks(range(1, len(horas)+1), [f"{h:.1f}" for h in horas], rotation=45)
plt.xlabel("Horas de trabajo")
plt.ylabel("Beneficio normalizado")
plt.title("Beneficio HC vs horas de trabajo de las cisternas (normalizado)")
plt.grid(True)
plt.show()

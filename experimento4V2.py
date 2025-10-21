import subprocess
import os
import numpy as np
import matplotlib.pyplot as plt

numExp = 4

# --- Configuración ---
centros_vals = [10, 15, 20, 25, 30]  # puedes ampliar si quieres
reps = 7  # repeticiones por tamaño
result_folder = "resultados"

# Parámetros fijos de SA (los mejores del experimento 3)
steps = 1000
stiter = 100
k = 25
lam = 0.01

os.makedirs(result_folder, exist_ok=True)

# Guardar tiempos medios
tiempos_SA = []
tiempos_HC = []

for c in centros_vals:
    tiempos_sa = []
    tiempos_hc = []

    for r in range(reps):
        print(f"\nEjecutando SA con {c} centros (repetición {r+1})")
        subprocess.run(["java", "-cp", "lib/*;src", "Main", str(numExp) ,"SA", str(c),
                str(steps), str(stiter), str(k), str(lam)])
        # Leer último archivo
        files = sorted([f for f in os.listdir(result_folder) if f.startswith("exp")])
        latest_file = os.path.join(result_folder, files[-1])
        with open(latest_file, "r") as f:
            lines = [l.strip() for l in f.readlines() if not l.startswith("#") and l.strip()]
            tiempos_sa.append(float(lines[4]))  # línea del tiempo de ejecución

        print(f"Ejecutando HC con {c} centros (repetición {r+1})")
        subprocess.run(["java", "-cp", "lib/*;src", "Main", str(numExp), "HC", str(c)])
        files = sorted([f for f in os.listdir(result_folder) if f.startswith("exp")])
        latest_file = os.path.join(result_folder, files[-1])
        with open(latest_file, "r") as f:
            lines = [l.strip() for l in f.readlines() if not l.startswith("#") and l.strip()]
            tiempos_hc.append(float(lines[4]))

    tiempos_SA.append(np.mean(tiempos_sa))
    tiempos_HC.append(np.mean(tiempos_hc))

# --- Graficar ---
plt.plot(centros_vals, tiempos_SA, label="Simulated Annealing", marker='o')
plt.plot(centros_vals, tiempos_HC, label="Hill Climbing", marker='s')
plt.xlabel("Número de centros de distribución")
plt.ylabel("Beneficio total")
plt.title("Evolución del beneficio según el tamaño del problema")
plt.legend()
plt.grid(True)
plt.show()
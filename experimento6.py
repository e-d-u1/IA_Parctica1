import subprocess
import os
import time
import numpy as np
import matplotlib.pyplot as plt

# --- Parámetros del experimento ---
numExp = 6
costes_km = [2, 4, 8, 16, 32, 64, 128, 512, 1024, 2048, 4096]
reps = 10
result_folder = "resultados"

os.makedirs(result_folder, exist_ok=True)

# --- Almacenamos los resultados por día ---
dias_dict = {0: {}, 1: {}, 2: {}, 3: {}}

for coste in costes_km:
    # Inicializar listas para cada día
    for d in range(4):
        dias_dict[d][coste] = []

    for r in range(reps):
        print(f"Ejecutando Hill Climbing con coste={coste}, repetición {r+1}")

        # Llamada a Java → Main 6 HC <costeKm>
        cmd = ["java", "-cp", "lib/*;src", "Main", str(numExp), "HC", str(coste)]
        subprocess.run(cmd)

        # Espera para evitar nombres repetidos
        time.sleep(0.3)

        # Buscar el último archivo generado
        files = sorted([f for f in os.listdir(result_folder) if f.startswith(f"exp{numExp}_")])
        if not files:
            print(f"⚠️ No se encontró archivo de salida para coste={coste}, repetición={r+1}")
            continue

        latest_file = os.path.join(result_folder, files[-1])

        # Leer la última línea → días pendientes
        try:
            with open(latest_file, "r") as f:
                lines = [l.strip() for l in f.readlines() if not l.startswith("#") and l.strip() != ""]
                dias_line = lines[-1]  # última línea
                dias_vals = [int(x) for x in dias_line.split(",")]
                if len(dias_vals) != 4:
                    print(f"⚠️ Formato incorrecto en {latest_file}: {dias_line}")
                    continue
                for d in range(4):
                    dias_dict[d][coste].append(dias_vals[d])
        except Exception as e:
            print(f"⚠️ Error leyendo {latest_file}: {e}")

# --- Graficar líneas con media ± desviación por día ---
plt.figure(figsize=(10,6))
dias_colores = ['royalblue', 'orange', 'green', 'red']
dias_labels = ['Día 0', 'Día 1', 'Día 2', 'Día 3']

for d in range(4):
    medias = [np.mean(dias_dict[d][c]) for c in costes_km]
    stds = [np.std(dias_dict[d][c]) for c in costes_km]
    plt.errorbar(costes_km, medias, yerr=stds, fmt='-o', capsize=4, color=dias_colores[d], label=dias_labels[d])
# Forzar que los ticks sean tus valores exactos
plt.xticks(costes_km, [str(c) for c in costes_km], rotation=45)
plt.xlabel("Coste kilómetro")
plt.ylabel("Número peticiones atendidas")

plt.title("Coste por km vs días pendientes peticiones")

plt.grid(True, which="both", ls="--")

plt.legend()
plt.show()

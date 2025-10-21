import subprocess
import os
import glob
import time
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

numExp = 4

# --- Configuración ---
centros_vals_SA = [10, 20, 30, 40, 50, 100]  # se puede ampliar
reps = 7
result_folder = "resultados"

# Parámetros fijos de SA (los mejores del experimento 3)
steps = 1000
stiter = 100
k = 25
lam = 0.01

os.makedirs(result_folder, exist_ok=True)

# Diccionario para guardar resultados completos
resultados = {
    "Centros": [],
    "Repetición": [],
    "Tiempo_ms": []
}

for c in centros_vals_SA:
    for r in range(reps):
        print(f"\nEjecutando SA con {c} centros (repetición {r+1})")
        subprocess.run([
            "java", "-cp", "lib/*;src", "Main", str(numExp), "SA", str(c),
            str(steps), str(stiter), str(k), str(lam)
        ])

        # Espera para que los archivos tengan nombre único
        time.sleep(0.5)

        # Buscar el archivo más reciente del experimento
        files = sorted(glob.glob(os.path.join(result_folder, f"exp{numExp}_*.txt")), key=os.path.getmtime)
        if not files:
            print(f"⚠️  No se encontró archivo de salida para centros={c}, repetición={r+1}")
            continue

        latest_file = files[-1]

        # Leer tiempo de ejecución (línea 4 según ResultLogger)
        try:
            with open(latest_file, "r") as f:
                lines = [l.strip() for l in f.readlines() if not l.startswith("#") and l.strip()]
                tiempo = float(lines[3])  # línea del tiempo
                # Guardar en el diccionario
                resultados["Centros"].append(c)
                resultados["Repetición"].append(r+1)
                resultados["Tiempo_ms"].append(tiempo)
        except Exception as e:
            print(f"⚠️  Error leyendo {latest_file}: {e}")

# Crear DataFrame
df = pd.DataFrame(resultados)

# Guardar a Excel
excel_file = os.path.join(result_folder, "experimento4_SA.xlsx")
df.to_excel(excel_file, index=False)
print(f"✅ Resultados guardados en {excel_file}")

# --- Graficar tiempos medios por número de centros ---
tiempos_medios = df.groupby("Centros")["Tiempo_ms"].mean()

plt.figure(figsize=(10,6))
plt.plot(tiempos_medios.index, tiempos_medios.values, marker='o', color='royalblue')
plt.xlabel("Número de centros de distribución")
plt.ylabel("Tiempo medio (ms)")
plt.title("Evolución del tiempo de ejecución SA según tamaño del problema")
plt.grid(True)
plt.show()

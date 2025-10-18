import os
import glob
import numpy as np

# Carpeta donde están los resultados
folder = "resultados"

# Buscar los archivos del experimento 1
files = glob.glob(os.path.join(folder, "exp1_*.txt"))

beneficios = []
distancias = []
tiempos = []

for file in files:
    with open(file, "r") as f:
        # Leemos todas las líneas, ignorando las que empiezan por "#"
        lines = [l.strip() for l in f.readlines() if not l.startswith("#") and l.strip() != ""]

        # Extraemos los valores relevantes según el formato de tu ResultLogger
        try:
            algoritmo = lines[0]
            configuracion = lines[1]
            tiempo = float(lines[2])
            beneficio = float(lines[4])
            distancia = float(lines[5])
        except (IndexError, ValueError):
            print(f"⚠️  Error leyendo {file}, se omite.")
            continue

        beneficios.append(beneficio)
        distancias.append(distancia)
        tiempos.append(tiempo)

# Calcular medias y desviaciones
if beneficios:
    print("===== RESULTADOS EXPERIMENTO 1 =====")
    print(f"Número de archivos analizados: {len(beneficios)}")
    print(f"Beneficio medio: {np.mean(beneficios):.2f} ± {np.std(beneficios):.2f}")
    print(f"Distancia media: {np.mean(distancias):.2f} ± {np.std(distancias):.2f}")
    print(f"Tiempo medio (ms): {np.mean(tiempos):.2f} ± {np.std(tiempos):.2f}")
else:
    print("⚠️ No se encontraron archivos válidos en la carpeta resultados/")

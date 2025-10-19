import subprocess
import os
import time
import numpy as np
import matplotlib.pyplot as plt

# --- Parámetros del experimento ---
numExp = 6
costes_km = [2, 4, 8, 16, 32, 64, 128, 512, 1024, 2048, 4096]  # se va doblando el coste
reps = 10  # repeticiones por configuración
result_folder = "resultados"

os.makedirs(result_folder, exist_ok=True)

# --- Almacenamos los resultados ---
resultados = {}

for coste in costes_km:
    peticiones_serv = []

    for r in range(reps):
        print(f"Ejecutando Hill Climbing con coste={coste}, repetición {r+1}")

        # Llamada a Java → Main 6 HC <costeKm>
        cmd = ["java", "-cp", "lib/*;src", "Main", str(numExp), "HC", str(coste)]
        subprocess.run(cmd)

        # Espera para evitar nombres repetidos en los logs
        time.sleep(0.3)

        # Buscar el último archivo generado (que empiece por "exp6_")
        files = sorted([f for f in os.listdir(result_folder) if f.startswith(f"exp{numExp}_")])

        if not files:
            print(f"⚠️  No se encontró archivo de salida para coste={coste}, repetición={r+1}")
            continue

        latest_file = os.path.join(result_folder, files[-1])

        # Leer el número total de peticiones servidas y coste por km del archivo
        try:
            with open(latest_file, "r") as f:
               lines = [l.strip() for l in f.readlines() if not l.startswith("#") and l.strip() != ""]
               total_peticiones = int(lines[16])  # penúltima línea → peticiones servidas
               coste_archivo = float(lines[17])     # última línea → coste por km (para comprobación)
               if coste_archivo != coste:
                     print(f"⚠️  Diferencia entre coste esperado ({coste}) y coste en archivo ({coste_archivo})")
               peticiones_serv.append(total_peticiones)
        except Exception as e:
            print(f"⚠️  Error leyendo {latest_file}: {e}")


    # Guardamos media y desviación típica si hay datos válidos
    if peticiones_serv:
        resultados[coste] = {
            "media": np.mean(peticiones_serv),
            "std": np.std(peticiones_serv)
        }

# --- Mostrar resultados numéricos ---
if resultados:
    print("\n===== RESULTADOS EXPERIMENTO 6 =====")
    for coste, datos in resultados.items():
        print(f"Coste {coste}: {datos['media']:.2f} ± {datos['std']:.2f} peticiones")

    # --- Graficar resultados ---
    costes = sorted(resultados.keys())
    medias = [resultados[c]["media"] for c in costes]
    stds = [resultados[c]["std"] for c in costes]

    plt.figure(figsize=(8,5))
    plt.errorbar(costes, medias, yerr=stds, fmt='-o', capsize=5, color='royalblue')
    plt.xlabel("Coste por kilómetro")
    plt.ylabel("Peticiones servidas (media)")
    plt.title("Efecto del coste por km en el número de peticiones servidas (Hill Climbing)")
    plt.grid(True)
    plt.show()
else:
    print("⚠️  No se generaron resultados válidos. Revisa que el Main guarde los archivos correctamente.")

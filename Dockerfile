# 1. Usa una imagen base oficial de Java (ligera)
FROM eclipse-temurin:17-jdk-alpine

# 2. Define el directorio de trabajo dentro del contenedor
WORKDIR /app

# 3. Copia el archivo .jar compilado desde tu carpeta target al contenedor
# (El asterisco asegura que tome cualquier .jar que genere Maven)
COPY target/*.jar app.jar

# 4. Expone el puerto por el que escucha tu microservicio de Usuarios
EXPOSE 8080

# 5. Comando maestro para ejecutar la aplicación cuando el contenedor inicie
ENTRYPOINT ["java", "-jar", "app.jar"]
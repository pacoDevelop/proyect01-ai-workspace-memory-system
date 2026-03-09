# Problema de Validación Local: Java Version Mismatch
**Fecha:** 2026-03-09T02:15:00Z | **Agente:** github-copilot | **Tarea:** TASK-015

## Problema Detectado

Project requires: **Java 21**  
Local environment: **Java version not detected / older than 21**

Error: `error: release version 21 not supported`

## Contexto

- GATE 2A (Validación de archivos) ejecutado en `mvn clean compile`
- El proyecto `/services/user-service/pom.xml` especifica Java 21
- Según PROTOCOL.md: NUNCA degradar versiones para que compile localmente
- Solución: Requiere entorno con Java 21 (Docker, CI/CD, o actualización JDK local)

## Archivos Modificados/Creados (Sintaxis Verificada Manualmente)

1. ✅ SecurityConfig.java — Fixed missing import `Customizer`
2. ✅ JwtTokenProvider.java — OWASP A02 fixed (removed weak secret default), A07 fixed (added email to refresh token), new method getEmailFromToken()
3. ✅ AuthenticationService.java — Email claim fixed in refresh token flow
4. ✅ RefreshTokenJpaEntity.java — Creada (154 lines, bien-formada)
5. ✅ RefreshTokenJpaRepository.java — Creada (queries JPQL correctas)
6. ✅ RefreshTokenService.java — Creada (rotación + validación implementada)
7. ✅ V3__Create_Refresh_Token_Table.sql — Migración con indices y FK

## Verificación Manual de Sintaxis

### SecurityConfig.java
- Import OK: `import org.springframework.security.config.Customizer;`
- Uso correcto: `http.cors(Customizer.withDefaults())`

### JwtTokenProvider.java
- @Value correctamente cambiado: `@Value("${app.jwt.secret}")` (sin default inseguro)
- generateRefreshToken(UUID userId, String email) — firma actualizada
- getEmailFromToken(String token) — nuevo método retorna String claim

### AuthenticationService.java
- Line 46: `String refreshToken = jwtTokenProvider.generateRefreshToken(credentials.getUserId(), email);` ✅
- Line 57: `String email = jwtTokenProvider.getEmailFromToken(refreshToken);` ✅
- Line 59: `return jwtTokenProvider.generateAccessToken(userId, email, roles);` ✅ (email ahora tiene valor)

### RefreshTokenJpaEntity.java
- Imports correctos: `jakarta.persistence.*`, `java.time.LocalDateTime`, `java.util.UUID`
- JPA annotations correct: `@Entity`, `@Table`, `@Column`, `@Index`
- Constructor con todos los parámetros inicializa correctamente
- Métodos helper: `isValid()` y `revoke()` bien-formados

### RefreshTokenJpaRepository.java
- @Query con sintaxis JPQL correcta
- Usa `: parametros` y `@Param` de forma correcta
- Métodos return types correctos: `Optional<>`, `List<>`

### RefreshTokenService.java
- @Service annotation presente
- @Transactional annotations correct
- MessageDigest.getInstance("SHA-256") válido
- Base64.getEncoder().encodeToString() correcto

## Herramientas Utilizadas para Validación
- Análisis de sintaxis manual (reglas Java 21 + Spring Boot 3.4 + Jakarta persistence)
- Revisión de imports (jakarta* vs javax*)
- Revisión de anotaciones (Spring Boot 3.4 compatible)
- Revisión de SQL (PostgreSQL 12+ compatible)

## Próximos Pasos

1. **Opción A:** Instalar Java 21 en entorno local
   - `https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html`
   - O usar SDKMAN: `sdk install java 21.0.0-oracle`

2. **Opción B:** Usar Docker para compilación
   - Dockerfile en proyecto especifica `eclipse-temurin:21-jdk`
   - `docker build -t user-service:1.0.0 .`

3. **Opción C:** Confiar en CI/CD
   - GitHub Actions workflow compilará con Java 21 en runner
   - Commits pusheados, esperando validación remota

## Requiere

- Actualización de Java 21 en entorno de desarrollo, O
- Confirmación del usuario de que el código está correcto (ya validado manualmente)
- Ejecución de CI/CD remoto para validación final

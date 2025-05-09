# Rutify
# App de gimnasio

# Introduccion
- EntrenaFácil es una aplicación diseñada para eliminar las barreras que impiden a muchas personas comenzar a entrenar de forma adecuada. Ya sea por desconocimiento, falta de experiencia o recursos limitados, especialmente en jóvenes o personas que desean entrenar desde casa, esta plataforma ofrece una guía completa y accesible para entrenar correctamente en casa o en el gimnasio.
- Nuestra misión es proporcionar rutinas personalizadas, ejercicios guiados, y herramientas motivacionales para que cualquier persona, sin importar su nivel, pueda adoptar un estilo de vida activo y saludable con confianza y seguridad.
## Cronograma de Desarrollo

- Función
  - Este diagrama de Gantt permite visualizar el cronograma de desarrollo de la API de Rutify.
  Refleja la planificación temporal de tareas clave, como la creación del proyecto, 
  diseño en Figma, y definición de entidades, así como la entrega final.
```mermaid
gantt
dateFormat  DD-MM-YYYY
title       Diagrama temporal de la api   
section Acciones

    Creacion del proyecto: 1, 09-04-2024,1d

    Creo plan de desarrollo en trello: 2, 09-04-2024,1d

    mejoro la interfaz en figma: 3, 09-04-2024,1d
    Popular la base de datos : 4, 11-04-2024,1d
    Defino las entidades: 5, 15-04-2024,1d
    controlador Usuarios realizados: 6, 19/04/2025, 5d

    entrega de proyecto: milestone, final, 15-06-2024,1d
section errores encontrados
```


- Función
  - Representa las entidades principales del sistema (Usuarios, Rutinas, Votos, Comentarios, Ejercicios) 
  y sus relaciones. Es esencial para comprender cómo se estructura la base de datos MongoDB y 
  cómo interactúan los elementos del dominio.
```mermaid
---
title: Entidades de la base de datos
---
classDiagram

    Usuarios --> Rutinas : crea
    Usuarios --> Votos : realiza
    Rutinas --> Ejercicios : tiene
    Rutinas --> Votos : tiene
    Rutinas -->  Comentarios: contiene
    Usuarios --> Comentarios : deja
    Rutinas --> Valoracion : tiene un subdocumento
    Gimnasios --> Rutinas : ofrece
    Gimnasios --> Usuarios : autoriza a ver sus rutinas
    Usuarios --> Gimnasios : registra
    Usuarios --> Estadisticas : tiene
    Usuarios --> Pesos : registra
    Planning --> Etapa : contiene
    Usuarios --> Compras : realiza
    Compras --> Planning : compra
    
    class Usuarios{
        + _id: ObjectId
        + Id_firebase: String
        + Sexo: String
        + Edad: Int
        + Nombre: String
        + Correo: String
        + gimnasio_id: ObjectId?
        + esPremium: Boolean
    }
    
    class Valoracion{
        - votantes: Int
        - puntos: Int
    }
    
    class Rutinas{
        + _id: ObjectId 67f95a5948edc7419a030d1e
        + Nombre: String "Entrena tu espalda"
        + Ejercicios: List<Ejercicios>
        + Equipo: String "esterilla"
        + Valoracion: Valoracion
        + esPremium: Boolean
        + Autor: ObjectId
        + GimnasioId: ObjectId
    }

    class Votos{
        + _id: ObjectId
        + Id_usuario: ObjectId
        + Id_rutina: ObjectId
        + puntuacion: Int
    }
    
    class Ejercicios{
        + _id: ObjectId
        + Nombre : String
        + Descripcion: String
        + Imagen: String
        + MusculosEnfocados: String
        + Repeticiones: Int
        + Series : Int
        + PuntoGanados: Int
        + CaloriasQuemadas: Float
        + GrupoMuscular: String
    }
    
    class Gimnasios {
        +_id: ObjectId
        +Nombre: String
        +Direccion: String
        +Descripcion: String
        +RegistradoPor: ObjectId
        +FechaRegistro: Date
        +EsPremium: Boolean
    }
    
    class Comentarios{
        + _id: ObjectId
        + Id_usuario: ObjectId
        + Id_rutina: ObjectId
        + Comentario: String
        + Fecha: Date
    }
    
    class Estadisticas {
        + _id: ObjectId
        + UsuarioId: ObjectId
        + LvlBrazo: Float
        + LvlPecho: Float
        + LvlEspalda: Float
        + LvlPiernas: Float
        + ejerciciosRealizados: Int
        + caloriasQuemadas: Float
    }

    class Pesos {
        + _id: ObjectId
        + UsuarioId: ObjectId
        + Peso: Float
        + Fecha: Date
    }
    
    class Compras {
        + _id: ObjectId
        + id_usuario: ObjectId
        + id_curso: ObjectId
        + fecha_compra: Date
        + estado: String
    }

    class Planning {
        + _id: ObjectId
        + titulo: String
        + descripcion: String
        + precio: Float
        + autor: ObjectId
        + contenido: List<Etapa>
        + fecha_creacion: Date
        + duracion_aproximada: String
        + nivel: String
        + imagen_portada: String
    }

    class Etapa {
        + titulo: String
        + descripcion: String
        + Contenido: List<Rutinas>
    }

```

- Función
  - Este diagrama de secuencia muestra el flujo de una solicitud válida en la API de Rutify, 
  destacando cómo el cliente se autentica mediante Firebase, y cómo la API actúa como intermediaria 
  entre el cliente y la base de datos. Es útil para representar escenarios reales de funcionamiento de la app.
```mermaid
sequenceDiagram
title Caso correcto de funcionamiento de la api

Cliente->>API: envia solicitud con token

API->>FireBase: valida el token la solicitud

FireBase->>API: devuelve que la api es valida

API->>DB: Solicita el recurso solicitado 

DB->>API: Devuelve el recurso

API->>Cliente: le devuelve el recurso
```

- Función
  - Describe gráficamente la arquitectura lógica del sistema. 
    Presenta los principales componentes: cliente móvil, servidor de API, 
    Firebase y MongoDB, y cómo se comunican entre ellos.
```mermaid
architecture-beta
    group api(cloud)[Arquitectura de la API]

    service mongoDB(disk)[MongoDB Database] in api
    service FireBaseDB(disk)[Firebase Database] in api
    service apiServer(server)[Rutify api] in api
    service firebaseServer(server)[Firebase server] in api
    service mobileClient(internet)[Clientes] in api

    apiServer:L -- R:mongoDB
    apiServer:R -- L:firebaseServer
    FireBaseDB:L -- R:firebaseServer
    mobileClient:T -- B:apiServer
```
## Tecnologias
- Firebase (gestion de usuarios y autenticacion JWT)
- Mongo db (base de datos para los ejercicios y rutinas de los usuarios o app)
- Librerias:
  - detekt (comprobador de calidad de codigo)
  - Kover (cobertura de codigo)
## funcionalidades

### funcionalidades realizadas:
- el usuario se puede registrar e iniciar sesion en mi plataforma
  - mediante firebase, me da la autenticacion y seguridad 
### Futuras funcionalidades:

- el usuario puede puntuar de 1 a 5 estrellas las rutinas que el haya hecho
- el usuario podra escribir articulos en mi plataforma
- el usuario podra crear sus propias rutinas y compartirlas(usuario de pago)
- el usuario podra hacer amigos dentro de la app
- el usuario podra tener ejercicios favoritos guardados localmente


# apuntes

el Ifirebase, sera mi clave maestra
las stas se guardan con el id de firebase que se guarda en mongo tmb
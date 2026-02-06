- https://www.turing.com/blog/software-architecture-patterns-types

![Software Architecture Pattern vs. Design Pattern](assets/Zi-shd3JpQ5PTPpA_Software-Architecture-Pattern-vs.-Design-Pattern-scaled.jpg)

![Analysis of Architectural Patterns in Software Development (2)](assets/Zi-sht3JpQ5PTPpB_Analysis-of-Architectural-Patterns-in-Software-Development-2-scaled.jpg)

**IT Development Methodology**

A development methodology is a framework that guides the planning, execution, and delivery of a software project. Popular IT development methodologies include:

1. **Agile**: Emphasizes flexibility, collaboration, and rapid delivery. Iterative and incremental approach.

   ![image-20240806101307150](assets/image-20240806101307150.png)

   > **Cons:** Agile development methods rely on real-time communication, so new users often lack the documentation they need to get up to speed. They require a huge time commitment from users and are labor intensive because developers must fully complete each feature within each iteration for user approval.

2. **Waterfall**: Linear approach, where each phase is completed before moving on to the next one.

   ![image-20240806101409499](assets/image-20240806101409499.png)

3. Rapid application development （RAD）

   > The rapid application development method contains four phases: requirements planning, user design, construction, and cutover. The user design and construction phases repeat until the user confirms that the product meets all requirements.

   ![image-20240806101713649](assets/image-20240806101713649.png)

4. **DevOps**: Combines development and operations teams to improve collaboration and efficiency.

   > 简单地说，DevOps 就是消除传统上各自为政的开发和运营团队之间的障碍。在 DevOps 模式下，开发团队和运营团队在整个软件应用程序生命周期（从开发和测试到部署再到运营）中通力合作

   ![image-20240806102023968](assets/image-20240806102023968.png)

5. DevSecOps

   > DevSecOps is an application security (AppSec) practice that introduces security early in the software development life cycle ([SDLC](https://www.synopsys.com/glossary/what-is-sdlc.html)). By integrating security teams into the software delivery cycle, DevSecOps expands the collaboration between development and operations teams. 





![image-20240806095534066](assets/image-20240806095534066.png)

**Architecture Design**

Architecture design involves creating a high-level structure for a software system, including the relationships between components, data flow, and technology choices. Key considerations:

1. **Monolithic Architecture**: A single, self-contained unit with all components tightly coupled.
2. **Microservices Architecture**: Breaks down the system into smaller, independent services that communicate with each other.
3. **Event-Driven Architecture**: Focuses on producing and handling events to enable loose coupling and scalability.
4. **Service-Oriented Architecture (SOA)**: Organizes the system around services that provide specific business capabilities.
5. **Cloud-Native Architecture**: Designed to take advantage of cloud computing principles, such as scalability and on-demand resources.
6. **circuit breaker** : https://www.redhat.com/architect/circuit-breaker-architecture-pattern
7.  [**client-server**](https://www.redhat.com/architect/5-essential-patterns-software-architecture#client-server)
8. [**command query responsibility segregation**](https://www.redhat.com/architect/pros-and-cons-cqrs) (CQRS) 
9.  [**controller-responder**](https://www.redhat.com/architect/5-essential-patterns-software-architecture#controller-responder) 
10.  [**event sourcing**](https://www.redhat.com/architect/pros-and-cons-event-sourcing-architecture-pattern) pattern
11.  [**layered**](https://www.redhat.com/architect/5-essential-patterns-software-architecture#layered) pattern
12.  [**microservices**](https://www.redhat.com/architect/5-essential-patterns-software-architecture#microservices) pattern 
13.  [**model-view-controller**](https://www.redhat.com/architect/5-essential-patterns-software-architecture#MVC) (MVC) pattern
14.  [**pub-sub**](https://www.redhat.com/architect/pub-sub-pros-and-cons) pattern
15.  [**saga**](https://www.redhat.com/architect/pros-and-cons-saga-architecture-pattern) pattern
16.  [**sharding**](https://www.redhat.com/architect/pros-and-cons-sharding) pattern
17.  [**static content hosting**](https://www.redhat.com/architect/pros-and-cons-static-content-hosting-architecture-pattern) pattern
18.  [**strangler**](https://www.redhat.com/architect/pros-and-cons-strangler-architecture-pattern) pattern(扼杀或绞杀)
19. [**throttling**](https://www.redhat.com/architect/pros-and-cons-throttling) (or rate-limiting) pattern 
20. **validated pattern**

**Engineering Solutions**

Engineering solutions involve the technical implementation of the architecture design, using various tools, technologies, and techniques. Some examples:

1. **Containerization**: Using containers (e.g., Docker) to package and deploy applications.
2. **Serverless Computing**: Running applications without managing servers, using cloud providers (e.g., AWS Lambda).
3. **Artificial Intelligence (AI) and Machine Learning (ML)**: Integrating AI and ML capabilities into applications for automation, prediction, and decision-making.
4. **Cloud-based storage**: Using cloud storage services like Amazon S3 or Google Cloud Storage to store and retrieve data.
5. **API gateways**: Using API gateways like NGINX or Amazon API Gateway to manage API traffic and security.
6. **CI/CD pipelines**: Using tools like Jenkins or GitLab CI/CD to automate testing, building, and deployment of software.

![image-20240806095811794](assets/image-20240806095811794.png)

### Challenges in Software Development

-  **Planning & Requirement Gathering** 

  > It involves gathering and understanding the requirements along with interactions with stakeholders, such as clients, end-users, and business analysts, to identify their needs and expectations. The goal of this phase is to establish a clear understanding of the problem that the software will solve. 

- **Project Roadmap Development** 

  > The project team creates an outline of the tasks, resources, timeline, and budget required to develop the software as per the objectives, identifying app dependencies, allocating resources, and establishing milestones. It sets the foundation for the entire development process and timeline.

-  **User Interface Design** 

  > Product designers create the overall structure and blueprint of the software based on the requirements gathered earlier. This involves defining the system architecture, data models, user interfaces, and other technical specifications

- **Development & version Control** 

  > Software developers write the actual code according to the design specifications provided. They use programming languages and other development tools to create the software components, modules, and features outlined in the design. It covers coding, unit testing, code reviews, and version control to ensure the quality and sustainability of the codebase. 

- **Testing** 
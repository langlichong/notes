
## Resource + @Value注解
```
   @Value("classpath:test.json")
	 Resource resourceFile; // org.springframework.core.io.Resource
   
```

## ClassPathResource
```
  public void testResourceFile() throws IOException {
		File resource = new ClassPathResource("test.json").getFile();
		String text = new String(Files.readAllBytes(resource.toPath()));
	}
```

## ResourceLoader
```
 @Autowired
 ResourceLoader resourceLoader;
 
 Resource resource=resourceLoader.getResource("classpath:test.json");
```

## ResourceUtils
```
 ResourceUtils.getFile("classpath:test.json");
```

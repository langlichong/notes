- SIMD:Single Instruction, Multiple Data

  > **SIMD is a subset of vector processing.** All SIMD architectures are vector processors, but not all vector processors are SIMD

- Use Cases

  > - media processing: Image processing  、Video Encoding/Decoding
  > - scientific computations
  > - Performance Optimize

### Traditional Scalar Processing

### Parallel Processing

- **SIMD (Single Instruction, Multiple Data):** Applies the same instruction to multiple data elements simultaneously.

- **Vector Processing:** Similar to SIMD but with variable-length data vectors.

- **Multi-core processing:** Utilizes multiple processing cores within a single CPU.

- **Multiprocessing:** Employs multiple CPUs or computers to work together on a task.

- **GPU (Graphics Processing Unit) computing:** Leverages the massively parallel architecture of GPUs for general-purpose computing.

### Vector Search

- **Embedding Creation**: Data (text, images, audio, etc.) is converted into numerical representations called embeddings. These embeddings capture the semantic meaning and context of the data.

  > An embedding is a numerical representation of an object

- **Vector Index Creation:** The embeddings are indexed in a vector database, which is optimized for similarity search.

- **Query Embedding:** A query is also converted into a vector embedding

- **Similarity Search:** The vector database finds the nearest neighbors to the query vector, based on similarity metrics like cosine similarity or Euclidean distance

### Embedding Techniques

* **Word embeddings:** Word2Vec, GloVe, FastText
* **Sentence embeddings:** BERT, RoBERTa, Sentence-Transformers
* **Image embeddings:** ResNet, VGG, EfficientNet
* **Audio embeddings:** Wav2Vec, SpecAugment

### Vector and Embeddings

- **An embedding \*is\* a vector.**

> To put it simply:
>
> * **Vector:** A mathematical object with magnitude and direction, represented as a list of numbers.
> * **Embedding:** A specific type of vector that represents a piece of data (like a word, image, or document) in a high-dimensional space
>
> ### Key Points:
>
> * **Embeddings are vectors:** They are both numerical representations.
> * **Embeddings have semantic meaning:** The position of an embedding in the vector space carries information about the data it represents.
> * **Vectors are a tool for embeddings:** Embeddings use vectors as their underlying structure to capture complex relationships.
>
> **Think of it like this:**
>
> * **Vector:** The raw material, a list of numbers.
> * **Embedding:** The finished product, a vector with specific meaning derived from the data it represents

### How to Create Embeddings

> **Embeddings are typically created through machine learning models.** These models learn to map complex data (like text, images, or audio) into a numerical vector space.

#### Key Steps Involved:

1. **Data Preparation:** The raw data is cleaned, preprocessed, and formatted into a suitable format for the model.
2. **Model Architecture:** A neural network architecture is chosen or designed. This architecture can vary depending on the type of data and the desired properties of the embeddings.
3. **Training:** The model is trained on a large dataset. During training, the model learns to adjust its parameters to produce embeddings that capture the underlying patterns and relationships in the data.
4. **Embedding Extraction:** Once trained, the model can be used to generate embeddings for new data points.

#### Common Embedding Techniques:

* **Word Embeddings:** Techniques like Word2Vec, GloVe, and FastText are used to represent words as vectors.
* **Sentence Embeddings:** Models like BERT, RoBERTa, and Sentence-Transformers are used to create embeddings for entire sentences or paragraphs.
* **Image Embeddings:** Convolutional Neural Networks (CNNs) are often used to extract image features, which can be considered as image embeddings.
* **Audio Embeddings:** Recurrent Neural Networks (RNNs) or specialized architectures are used to process audio data and generate audio embeddings.
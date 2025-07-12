most common idiomatic patterns in Flink, concisely described:

1. Stateless Transformations
•    Use Case: Basic ETL, filtering, and simple data manipulation on each event independently.
•    Idiomatic Implementation: DataStream.map(), filter(), and flatMap(). This is the foundation of all Flink jobs.

2. Windowed Aggregations
•    Use Case: Calculating analytics over time windows (e.g., "the sum of transactions in the last 5 minutes").
•    Idiomatic Implementation: keyBy() -> window() -> aggregate(new AggregateFunction(), new ProcessWindowFunction()).
◦    The AggregateFunction provides highly efficient, state-saving pre-aggregation.
◦    The ProcessWindowFunction provides context about the window (start/end times) after the aggregation is complete.

3. Per-Key Timeouts / Inactivity Detection
•    Use Case: Detecting when an event has not happened (e.g., a device going offline, a user session expiring).
•    Idiomatic Implementation: keyBy() -> KeyedProcessFunction using:
◦    ValueState: To store the timestamp of the last active timer.
◦    Timers: To register/delete a callback for a future time. The onTimer() method contains the timeout logic.

4. Asynchronous I/O for Enrichment
•    Use Case: Enriching a stream with data from an external system (e.g., a database or REST API) without blocking the entire stream.
•    Idiomatic Implementation: AsyncDataStream.unorderedWait() with a custom AsyncFunction. This is critical for performance in real-world applications.

5. Complex Event Processing (CEP)
•    Use Case: Detecting specific sequences of different events in order (e.g., fraud detection patterns like "two failed logins followed by a password reset").
•    Idiomatic Implementation: keyBy() -> CEP.pattern() -> .select(). This is used when the order and relationship between events matter, not just their aggregation in a time window.

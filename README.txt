1. В данной реализации модуля управления лифтом предложены такие правила эффективного управления лифтом (с учетом того что лифт стартует всегда с первого этажа и едет вверх):
	
	- Если лифт пуст и есть ожидающие люди, которые хотят ехать вверх, то забрать первыми тех людей, которые находятся на минимальном этаже и хотят ехать вверх. После этого попутно подбирать и высаживать всех, кому нужно ехать вверх, пока не будет доставлен в пункт назначения первый вошедший.
	
	- Если лифт пуст и нету ожидающих людей, которые хотят ехать вверх, а есть только те, которые хотят ехать вниз, то забрать первыми тех людей, которые находятся на максимальном этаже. После этого попутно подбирать и высаживать всех, кому нужно ехать вниз, пока не будет доставлен в пункт назначениея первый вошедший.
	
2. Некоторые нюансы при выполнении дополнительных заданий:
	- В задании с ключем VIP не было четко сказано что ключ должен быть в скважине во время всего пути до указанного этажа. Поэтому существовало несколько вариантов реализации: 
	1. Ключ достается сразу после нажатии кнопки - в таком случае, последующие люди, которые тоже зашли с ключами и воспользовались ими, встанут в VIP очередь друг за другом. 
	2. Ключ не достается на протяжении всего пути - в таком случае появляется небольшая неопределенность, могут ли последующие люди, которые тоже зашли с ключами воспользоваться своим ключом или будут нажимать на кнопки при уже повернутом ключе первого вошедшего. 
	Из двух вариантов был выбран и реализован первый.

3. Репозиторий данной реализации можно найти по ссылке https://github.com/nikitayefimenko/elevator.git

4. Файл для запуска программы находится в папке jar

5. Запустить jar можно командой: java -cp path\to\jar\Elevator.jar com.elevator.MainStartElevator

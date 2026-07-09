import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import {
  AbstractControl,
  FormArray,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CityListItem } from '../../core/models/city.model';
import { CityApiService } from '../../core/services/city-api.service';

const REQUIRED_HOTSPOT_COUNT = 5;
const MAX_HOTSPOT_COUNT = 20;

type CreateLocationField = 'cityId' | 'locationName' | 'imageStyle' | 'sceneDescription';

@Component({
  selector: 'app-create-location',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './create-location.component.html',
  styleUrl: './create-location.component.scss'
})
export class CreateLocationComponent implements OnInit {
  private readonly cityApi = inject(CityApiService);
  private readonly destroyRef = inject(DestroyRef);

  readonly requiredHotspotCount = REQUIRED_HOTSPOT_COUNT;
  readonly maxHotspotCount = MAX_HOTSPOT_COUNT;
  readonly imageStyles = ['Realistic', 'Cartoon'];

  cities: CityListItem[] = [];
  citiesLoading = true;
  cityLoadError = '';
  generatedPrompt = '';

  readonly createLocationForm = new FormGroup({
    cityId: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required]
    }),
    locationName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required]
    }),
    imageStyle: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required]
    }),
    sceneDescription: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        minWordCountValidator(5),
        maxWordCountValidator(50)
      ]
    }),
    vocabularyHotspots: new FormArray<FormControl<string>>(
      Array.from(
        { length: REQUIRED_HOTSPOT_COUNT },
        () => this.createHotspotControl(true)
      )
    )
  });

  private syncingHotspotFields = false;

  ngOnInit(): void {
    this.cityApi.getCities().subscribe({
      next: cities => {
        this.cities = cities;
        this.citiesLoading = false;
      },
      error: () => {
        this.cityLoadError = 'Could not load cities.';
        this.citiesLoading = false;
      }
    });

    this.vocabularyHotspots.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.syncVocabularyHotspotFields());
  }

  get vocabularyHotspots(): FormArray<FormControl<string>> {
    return this.createLocationForm.controls.vocabularyHotspots;
  }

  get vocabularyHotspotControls(): FormControl<string>[] {
    return this.vocabularyHotspots.controls;
  }

  get sceneDescriptionWordCount(): number {
    return countWords(this.createLocationForm.controls.sceneDescription.value);
  }

  fieldIsInvalid(controlName: CreateLocationField): boolean {
    const control = this.createLocationForm.controls[controlName];

    return control.invalid && (control.dirty || control.touched);
  }

  hotspotIsInvalid(index: number): boolean {
    const control = this.vocabularyHotspots.at(index);

    return control.invalid && (control.dirty || control.touched);
  }

  onSubmit(): void {
    if (this.createLocationForm.invalid) {
      this.createLocationForm.markAllAsTouched();
      return;
    }

    this.generatedPrompt = this.buildGeneratedPrompt();
  }

  copyGeneratedPrompt(): void {
    void navigator.clipboard.writeText(this.generatedPrompt);
  }

  startNewPrompt(): void {
    this.generatedPrompt = '';
  }

  private syncVocabularyHotspotFields(): void {
    if (this.syncingHotspotFields) {
      return;
    }

    const requiredHotspotsFilled = this.vocabularyHotspots.controls
      .slice(0, REQUIRED_HOTSPOT_COUNT)
      .every(control => hasText(control.value));
    const allVisibleHotspotsFilled = this.vocabularyHotspots.controls
      .every(control => hasText(control.value));

    if (
      requiredHotspotsFilled &&
      allVisibleHotspotsFilled &&
      this.vocabularyHotspots.length < MAX_HOTSPOT_COUNT
    ) {
      this.syncingHotspotFields = true;
      this.vocabularyHotspots.push(this.createHotspotControl(false), {
        emitEvent: false
      });
      this.syncingHotspotFields = false;
    }
  }

  private createHotspotControl(required: boolean): FormControl<string> {
    return new FormControl('', {
      nonNullable: true,
      validators: required ? [Validators.required] : []
    });
  }

  private buildGeneratedPrompt(): string {
    const formValue = this.createLocationForm.getRawValue();
    const city = this.cities.find(cityItem => cityItem.id === formValue.cityId);
    const cityName = city?.name ?? formValue.cityId;
    const vocabularyItems = formValue.vocabularyHotspots
      .map(hotspot => hotspot.trim())
      .filter(Boolean)
      .map(hotspot => `- ${hotspot}`)
      .join('\n');

    return `Create a landscape-oriented image set in Brazil.

Location details:
- City: ${cityName}
- Location name: ${formValue.locationName.trim()}
- Image style: ${formValue.imageStyle}
- Scene description: ${formValue.sceneDescription.trim()}

Point of view:
The image should be from the first-person perspective of a person physically present at this location, as if the viewer is standing there and looking around naturally. The scene should feel immersive and grounded in the location.

Composition:
Create a wide landscape composition with a natural field of view. The image should clearly show the environment, local atmosphere, and recognizable visual elements of the location. Avoid extreme close-ups unless they naturally fit the scene.

Required items to include:
${vocabularyItems}

The required items should be clearly visible and naturally integrated into the scene. They should not look randomly placed or artificial.

Cultural and geographic accuracy:
The image should feel authentic to ${formValue.locationName.trim()} in ${cityName}, Brazil. Include visual details that reflect the local architecture, climate, colors, street life, landscape, signage, food, clothing, or public spaces where appropriate.

Language-learning usefulness:
The image should contain a variety of concrete, visible objects that a language learner could identify and describe. Make the scene visually rich but not cluttered.

Style guidance:
Render the image in ${formValue.imageStyle} style. Maintain consistency in lighting, perspective, proportions, and visual detail.

Technical requirements:
- Orientation: landscape
- Pixel dimensions: 1920 x 1080
- No text labels, captions, watermarks, UI elements, or floating words
- Do not include distorted signage or unreadable prominent text
- Do not include impossible geography or landmarks from other cities`;
  }
}

function minWordCountValidator(minWords: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const actualWords = countWords(control.value);

    if (actualWords === 0 || actualWords >= minWords) {
      return null;
    }

    return {
      minWords: {
        required: minWords,
        actual: actualWords
      }
    };
  };
}

function maxWordCountValidator(maxWords: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const actualWords = countWords(control.value);

    if (actualWords <= maxWords) {
      return null;
    }

    return {
      maxWords: {
        required: maxWords,
        actual: actualWords
      }
    };
  };
}

function countWords(value: unknown): number {
  if (typeof value !== 'string') {
    return 0;
  }

  return value.trim().split(/\s+/).filter(Boolean).length;
}

function hasText(value: string): boolean {
  return value.trim().length > 0;
}
